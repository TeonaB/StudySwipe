package com.example.studyswipe.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyswipe.db.AppDatabase
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    // Relational Flow mapping users and their subjects
    val allUsers: StateFlow<List<User>> = subjectDao.getUsersWithSubjects()
        .map { entities -> entities.map { it.toUser() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Real-time Matches and Chat state streams
    private val _matches = MutableStateFlow<List<MatchEntity>>(emptyList())
    val matches = _matches.asStateFlow()

    private val _activeChatMessages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val activeChatMessages = _activeChatMessages.asStateFlow()

    var activeMatchId: String? = null
        private set

    private var messagesJob: kotlinx.coroutines.Job? = null

    init {
        // Automatically sync matches when the logged-in user changes
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    chatDao.getMatchesForUser(user.id).collect {
                        _matches.value = it
                    }
                } else {
                    _matches.value = emptyList()
                    _activeChatMessages.value = emptyList()
                    activeMatchId = null
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
            _currentUser.value = userWithSubjects?.toUser() ?: newUser
            _registerState.value = AuthResult.Success
        }
    }

    fun login(email: String, password: String) {
        _loginState.value = AuthResult.Loading

        viewModelScope.launch {
            val userWithSubjects = subjectDao.getUserWithSubjectsByEmail(email.trim().lowercase())

            if (userWithSubjects != null && userWithSubjects.user.password == password) {
                _currentUser.value = userWithSubjects.toUser()
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
        _currentUser.value = null
        _loginState.value = AuthResult.Idle
        _registerState.value = AuthResult.Idle
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
    subjects = subjects.mapNotNull { entity ->
        Subject.entries.find { it.name == entity.id }
    }.toSet(),
    bio = user.bio,
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
    isProfileComplete = isProfileComplete
)
