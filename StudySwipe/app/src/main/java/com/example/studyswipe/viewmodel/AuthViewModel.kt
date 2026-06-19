package com.example.studyswipe.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyswipe.db.AppDatabase
import com.example.studyswipe.db.DislikeEntity
import com.example.studyswipe.db.LikeEntity
import com.example.studyswipe.db.MatchEntity
import com.example.studyswipe.db.MessageEntity
import com.example.studyswipe.db.UserEntity
import com.example.studyswipe.db.UserSubjectEntity
import com.example.studyswipe.db.UserWithSubjects
import com.example.studyswipe.model.Subject
import com.example.studyswipe.model.User
import com.example.studyswipe.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.studyswipe.network.RetrofitClient
import com.example.studyswipe.network.dto.toUser


sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
    object Idle : AuthResult()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val userDao = AppDatabase.getInstance(application).userDao()
    private val subjectDao = AppDatabase.getInstance(application).subjectDao()
    private val chatDao = AppDatabase.getInstance(application).chatDao()

    private val _loginState = MutableStateFlow<AuthResult>(AuthResult.Idle)
    private val _registerState = MutableStateFlow<AuthResult>(AuthResult.Idle)
    private val _currentUser = MutableStateFlow<User?>(null)

    val loginState: StateFlow<AuthResult> = _loginState.asStateFlow()
    val registerState: StateFlow<AuthResult> = _registerState.asStateFlow()
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // HTTP API state for remote users
    private val _apiUsers = MutableStateFlow<List<User>>(emptyList())
    val apiUsers: StateFlow<List<User>> = _apiUsers.asStateFlow()

    private val _apiLoading = MutableStateFlow(false)
    val apiLoading: StateFlow<Boolean> = _apiLoading.asStateFlow()

    private val _apiError = MutableStateFlow<String?>(null)
    val apiError: StateFlow<String?> = _apiError.asStateFlow()

    private val _apiTotalPages = MutableStateFlow(1)
    val apiTotalPages: StateFlow<Int> = _apiTotalPages.asStateFlow()

    private val _apiCurrentPage = MutableStateFlow(1)
    val apiCurrentPage: StateFlow<Int> = _apiCurrentPage.asStateFlow()

    fun fetchUsersFromApi(page: Int, role: UserRole) {
        _apiLoading.value = true
        _apiError.value = null
        _apiCurrentPage.value = page
        
        viewModelScope.launch {
            try {
                val response = RetrofitClient.usersApi.getUsers(page = page, perPage = 5)
                val mappedUsers = response.data.map { dto ->
                    dto.toUser(role)
                }
                
                // Save to local Room DB on a background thread
                withContext(Dispatchers.IO) {
                    mappedUsers.forEach { user ->
                        userDao.insert(user.toEntity())
                        subjectDao.deleteUserSubjects(user.id)
                        user.subjects.forEach { subject ->
                            subjectDao.insertUserSubject(UserSubjectEntity(user.id, subject.name))
                        }
                    }
                }
                
                _apiUsers.value = mappedUsers
                _apiTotalPages.value = response.totalPages
            } catch (e: Exception) {
                e.printStackTrace()
                _apiError.value = e.localizedMessage ?: "A apărut o eroare la descărcarea utilizatorilor"
                _apiUsers.value = emptyList()
            } finally {
                _apiLoading.value = false
            }
        }
    }


    // Relational Flow mapping users and their subjects
    val allUsers: StateFlow<List<User>> = subjectDao.getUsersWithSubjects()
        .map { entities -> entities.map { it.toUser() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _likesFromMe = MutableStateFlow<List<LikeEntity>>(emptyList())
    private val _likesToMe = MutableStateFlow<List<LikeEntity>>(emptyList())
    private val _dislikesFromMe = MutableStateFlow<List<DislikeEntity>>(emptyList())

    // Real-time Matches and Chat state streams
    private val _matches = MutableStateFlow<List<MatchEntity>>(emptyList())
    val matches = _matches.asStateFlow()

    private val _activeChatMessages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val activeChatMessages = _activeChatMessages.asStateFlow()

    var activeMatchId: String? = null
        private set

    private var messagesJob: kotlinx.coroutines.Job? = null

    private val swipeDataFlow = combine(
        _likesFromMe,
        _likesToMe,
        _dislikesFromMe,
        matches
    ) { likesFrom, likesTo, dislikesFrom, activeMatches ->
        SwipeData(likesFrom, likesTo, dislikesFrom, activeMatches)
    }

    val swipeCandidates: StateFlow<List<User>> = combine(
        allUsers,
        currentUser,
        swipeDataFlow
    ) { all, current, swipeData ->
        if (current == null) return@combine emptyList<User>()

        val likedIds = swipeData.likesFromMe.map { it.likedId }.toSet()
        val dislikedIds = swipeData.dislikesFromMe.map { it.dislikedId }.toSet()
        val matchedUserIds = swipeData.matches.map { match ->
            if (match.user1Id == current.id) match.user2Id else match.user1Id
        }.toSet()

        val likersToMeIds = swipeData.likesToMe.map { it.likerId }.toSet()

        all.filter { user ->
            if (user.id == current.id) return@filter false
            if (user.id in likedIds) return@filter false
            if (user.id in dislikedIds) return@filter false
            if (user.id in matchedUserIds) return@filter false

            when (current.role) {
                UserRole.STUDENT -> {
                    (user.role == UserRole.TUTOR || user.role == UserRole.BOTH) &&
                            user.subjects.intersect(current.subjects).isNotEmpty()
                }
                UserRole.TUTOR -> {
                    (user.role == UserRole.STUDENT || user.role == UserRole.BOTH) && user.id in likersToMeIds
                }
                UserRole.BOTH -> {
                    val teachesMySubjects = (user.role == UserRole.TUTOR || user.role == UserRole.BOTH) &&
                            user.subjects.intersect(current.subjects).isNotEmpty()
                    val studentLikedMe = (user.role == UserRole.STUDENT || user.role == UserRole.BOTH) &&
                            user.id in likersToMeIds
                    teachesMySubjects || studentLikedMe
                }
                UserRole.ADMIN -> false
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Load logged in user from SharedPreferences
        val prefs = application.getSharedPreferences("StudySwipePrefs", Context.MODE_PRIVATE)
        val savedUserId = prefs.getString("logged_in_user_id", null)
        if (savedUserId != null) {
            viewModelScope.launch {
                val userWithSubjects = subjectDao.getUserWithSubjectsById(savedUserId)
                if (userWithSubjects != null) {
                    _currentUser.value = userWithSubjects.toUser()
                }
            }
        }

        // Automatically sync matches, likes, and dislikes when the logged-in user changes
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    launch {
                        chatDao.getMatchesForUser(user.id).collect {
                            _matches.value = it
                        }
                    }
                    launch {
                        chatDao.getLikesForLiker(user.id).collect {
                            _likesFromMe.value = it
                        }
                    }
                    launch {
                        chatDao.getLikesForLiked(user.id).collect {
                            _likesToMe.value = it
                        }
                    }
                    launch {
                        chatDao.getDislikesForDisliker(user.id).collect {
                            _dislikesFromMe.value = it
                        }
                    }
                } else {
                    _matches.value = emptyList()
                    _activeChatMessages.value = emptyList()
                    activeMatchId = null
                    _likesFromMe.value = emptyList()
                    _likesToMe.value = emptyList()
                    _dislikesFromMe.value = emptyList()
                }
            }
        }
    }

    fun register(name: String, email: String, password: String, role: UserRole) {
        _registerState.value = AuthResult.Loading

        viewModelScope.launch {
            val existing = userDao.getByEmail(email.trim().lowercase())
            if (existing != null) {
                _registerState.value = AuthResult.Error("Un cont cu acest email există deja!")
                return@launch
            }

            val newUser = User(
                name = name.trim(),
                email = email.trim().lowercase(),
                password = password,
                role = role
            )
            userDao.insert(newUser.toEntity())
            
            // Fetch registered UserWithSubjects (empty subjects list initially)
            val userWithSubjects = subjectDao.getUserWithSubjectsById(newUser.id)
            val user = userWithSubjects?.toUser() ?: newUser
            
            val prefs = getApplication<Application>().getSharedPreferences("StudySwipePrefs", Context.MODE_PRIVATE)
            prefs.edit().putString("logged_in_user_id", user.id).apply()

            _currentUser.value = user
            _registerState.value = AuthResult.Success
        }
    }

    fun login(email: String, password: String) {
        _loginState.value = AuthResult.Loading

        viewModelScope.launch {
            val userWithSubjects = subjectDao.getUserWithSubjectsByEmail(email.trim().lowercase())

            if (userWithSubjects != null && userWithSubjects.user.password == password) {
                val user = userWithSubjects.toUser()
                val prefs = getApplication<Application>().getSharedPreferences("StudySwipePrefs", Context.MODE_PRIVATE)
                prefs.edit().putString("logged_in_user_id", user.id).apply()

                _currentUser.value = user
                _loginState.value = AuthResult.Success
            } else {
                _loginState.value = AuthResult.Error("Email sau parolă incorectă!")
            }
        }
    }

    fun saveProfile(subjects: Set<Subject>, bio: String) {
        viewModelScope.launch {
            val current = _currentUser.value ?: return@launch
            val updatedUser = current.copy(
                subjects = subjects,
                bio = bio.trim(),
                isProfileComplete = true
            )

            // Update basic user properties
            userDao.update(updatedUser.toEntity())

            // Sync subjects Many-to-Many junction table
            subjectDao.deleteUserSubjects(current.id)
            subjects.forEach { subject ->
                subjectDao.insertUserSubject(UserSubjectEntity(current.id, subject.name))
            }

            _currentUser.value = updatedUser
        }
    }

    fun updateProfile(
        name: String,
        email: String,
        password: String,
        subjects: Set<Subject>,
        bio: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val current = _currentUser.value ?: return@launch
            val emailClean = email.trim().lowercase()
            
            // Check if email changed and is already taken
            if (emailClean != current.email) {
                val existing = userDao.getByEmail(emailClean)
                if (existing != null) {
                    withContext(Dispatchers.Main) {
                        onError("Un cont cu acest email există deja!")
                    }
                    return@launch
                }
            }

            val updatedUser = current.copy(
                name = name.trim(),
                email = emailClean,
                password = password,
                subjects = subjects,
                bio = bio.trim()
            )

            try {
                userDao.update(updatedUser.toEntity())

                // Sync subjects Many-to-Many junction table
                subjectDao.deleteUserSubjects(current.id)
                subjects.forEach { subject ->
                    subjectDao.insertUserSubject(UserSubjectEntity(current.id, subject.name))
                }

                _currentUser.value = updatedUser
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError("Eroare la actualizarea profilului: ${e.localizedMessage}")
                }
            }
        }
    }

    fun deleteUserByAdmin(userId: String, onCompleted: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                // Delete user relations and junction table entries
                subjectDao.deleteUserSubjects(userId)
                chatDao.deleteLikesForUser(userId)
                chatDao.deleteDislikesForUser(userId)
                chatDao.deleteMatchesForUser(userId)
                
                // Delete user record
                userDao.deleteById(userId)
                
                // Also remove from the apiUsers state flow in-memory
                _apiUsers.value = _apiUsers.value.filter { it.id != userId }
                
                withContext(Dispatchers.Main) {
                    onCompleted()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateUserProfileByAdmin(
        targetUserId: String,
        name: String,
        email: String,
        password: String,
        subjects: Set<Subject>,
        bio: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val emailClean = email.trim().lowercase()
            
            // Check if email changed and is already taken by some OTHER user
            val existing = userDao.getByEmail(emailClean)
            if (existing != null && existing.id != targetUserId) {
                withContext(Dispatchers.Main) {
                    onError("Un cont cu acest email există deja!")
                }
                return@launch
            }

            val targetEntity = userDao.getById(targetUserId) ?: return@launch
            val updatedEntity = targetEntity.copy(
                name = name.trim(),
                email = emailClean,
                password = password,
                subjects = subjects,
                bio = bio.trim()
            )

            try {
                userDao.update(updatedEntity)

                // Sync subjects Many-to-Many junction table
                subjectDao.deleteUserSubjects(targetUserId)
                subjects.forEach { subject ->
                    subjectDao.insertUserSubject(UserSubjectEntity(targetUserId, subject.name))
                }

                // Also update the apiUsers state flow in-memory
                _apiUsers.value = _apiUsers.value.map {
                    if (it.id == targetUserId) {
                        it.copy(
                            name = name.trim(),
                            email = emailClean,
                            password = password,
                            subjects = subjects,
                            bio = bio.trim()
                        )
                    } else it
                }

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError("Eroare la actualizarea utilizatorului: ${e.localizedMessage}")
                }
            }
        }
    }

    // Matches and messaging logic
    fun selectMatch(matchId: String) {
        activeMatchId = matchId
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            chatDao.getMessagesForMatch(matchId).collect {
                _activeChatMessages.value = it
            }
        }
    }

    fun sendMessage(content: String) {
        val matchId = activeMatchId ?: return
        val senderId = _currentUser.value?.id ?: return
        if (content.isBlank()) return

        viewModelScope.launch {
            val message = MessageEntity(
                matchId = matchId,
                senderId = senderId,
                content = content.trim()
            )
            chatDao.insertMessage(message)
        }
    }

    fun startChat(otherUserId: String, onMatchCreated: (String) -> Unit) {
        val currentUserId = _currentUser.value?.id ?: return
        viewModelScope.launch {
            val existingMatch = chatDao.getMatchBetweenUsers(currentUserId, otherUserId)
            if (existingMatch != null) {
                onMatchCreated(existingMatch.id)
            } else {
                val newMatch = MatchEntity(user1Id = currentUserId, user2Id = otherUserId)
                chatDao.insertMatch(newMatch)
                onMatchCreated(newMatch.id)
            }
        }
    }

    suspend fun getUserById(userId: String): User? {
        val userWithSubjects = subjectDao.getUserWithSubjectsById(userId)
        return userWithSubjects?.toUser()
    }

    fun logout() {
        val prefs = getApplication<Application>().getSharedPreferences("StudySwipePrefs", Context.MODE_PRIVATE)
        prefs.edit().remove("logged_in_user_id").apply()
        _currentUser.value = null
        _loginState.value = AuthResult.Idle
        _registerState.value = AuthResult.Idle
    }

    fun fetchSingleUserDetail(userId: String) {
        viewModelScope.launch {
            try {
                val numericId = userId.substringAfterLast("-").toLongOrNull()
                if (numericId != null) {
                    val response = RetrofitClient.usersApi.getUserById(numericId)
                    println("Fetched single user from API: ${response.data.firstName} ${response.data.lastName}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun likeUser(candidateId: String, onMatchCreated: (String) -> Unit = {}, onCompleted: () -> Unit = {}) {
        val currentUserId = _currentUser.value?.id ?: return
        val currentUserRole = _currentUser.value?.role ?: return

        viewModelScope.launch {
            try {
                val hasLikedMe = chatDao.getLike(likerId = candidateId, likedId = currentUserId) != null

                val shouldMatch = when (currentUserRole) {
                    UserRole.STUDENT -> hasLikedMe
                    UserRole.TUTOR -> true
                    UserRole.BOTH -> hasLikedMe
                    UserRole.ADMIN -> false
                }

                if (shouldMatch) {
                    val match = MatchEntity(user1Id = currentUserId, user2Id = candidateId)
                    chatDao.insertMatch(match)
                    // Delete likes in both directions to ensure no data is left behind in liked section
                    chatDao.deleteLike(likerId = candidateId, likedId = currentUserId)
                    chatDao.deleteLike(likerId = currentUserId, likedId = candidateId)
                    
                    withContext(Dispatchers.Main) {
                        onMatchCreated(match.id)
                    }
                } else {
                    chatDao.insertLike(LikeEntity(likerId = currentUserId, likedId = candidateId))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                withContext(Dispatchers.Main) {
                    onCompleted()
                }
            }
        }
    }

    fun dislikeUser(candidateId: String, onCompleted: () -> Unit = {}) {
        val currentUserId = _currentUser.value?.id ?: return

        viewModelScope.launch {
            try {
                chatDao.insertDislike(DislikeEntity(dislikerId = currentUserId, dislikedId = candidateId))
                chatDao.deleteLike(likerId = candidateId, likedId = currentUserId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                withContext(Dispatchers.Main) {
                    onCompleted()
                }
            }
        }
    }

    fun deleteMatch(matchId: String) {
        viewModelScope.launch {
            chatDao.deleteMatch(matchId)
        }
    }

    fun resetLoginState() { _loginState.value = AuthResult.Idle }
    fun resetRegisterState() { _registerState.value = AuthResult.Idle }
}

// Relational converters
fun UserWithSubjects.toUser() = User(
    id = user.id,
    name = user.name,
    email = user.email,
    password = user.password,
    role = user.role,
    subjects = user.subjects,
    bio = user.bio,
    avatarUrl = user.avatarUrl,
    isProfileComplete = user.isProfileComplete
)

fun User.toEntity() = UserEntity(
    id = id,
    name = name,
    email = email,
    password = password,
    role = role,
    subjects = subjects, // kept for backwards compatibility in UserDao
    bio = bio,
    avatarUrl = avatarUrl,
    isProfileComplete = isProfileComplete
)

fun UserEntity.toUser() = User(
    id = id,
    name = name,
    email = email,
    password = password,
    role = role,
    subjects = subjects,
    bio = bio,
    avatarUrl = avatarUrl,
    isProfileComplete = isProfileComplete
)

private data class SwipeData(
    val likesFromMe: List<LikeEntity>,
    val likesToMe: List<LikeEntity>,
    val dislikesFromMe: List<DislikeEntity>,
    val matches: List<MatchEntity>
)
