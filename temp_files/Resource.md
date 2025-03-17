classDiagram
direction BT
class ApiKeyFilter {
  + ApiKeyFilter() 
  - String API_KEY_HEADER
  - String API_KEY
  # doFilterInternal(HttpServletRequest, HttpServletResponse, FilterChain) void
}
class ApiProperties {
  + ApiProperties() 
  - List~CorsData~ crossOrigin
  - List~String~ whitelist
  + setWhitelist(List~String~) void
  + setCrossOrigin(List~CorsData~) void
  + getWhitelist() List~String~
  + getCrossOrigin() List~CorsData~
}
class ApiRepoController {
  + ApiRepoController(RepoService) 
  - RepoService repoService
  + getRepos() ResponseEntity~RestResponse~List~JsonRepo~~~
}
class ApiResourceController {
  + ApiResourceController(ResourceService) 
  - Logger log
  - ResourceService resourceService
  + updateResource(ResourceUpdateRequest) ResponseEntity~RestResponse~Void~~
  + getTags(String) ResponseEntity~RestResponse~List~Tag~~~
  + addTag(String, String, String) ResponseEntity~RestResponse~Void~~
  + moveResource(String, String, String, String, String) ResponseEntity~RestResponse~Void~~
  + removeResource(String, String) ResponseEntity~RestResponse~Void~~
  + insertResource(String, String, String, String, List~String~, String) ResponseEntity~RestResponse~Void~~
  + getResources(ResourceRequest) ResponseEntity~RestResponse~Map~String, List~JsonResource~~~~
  + removeTag(String, String) ResponseEntity~RestResponse~Void~~
  + setEditing(String, String, String) ResponseEntity~RestResponse~Void~~
  + getFiletree(ResourceRequest) ResponseEntity~RestResponse~Map~String, JsonFileTree~~~
  + isBeingEdited(String, String) ResponseEntity~RestResponse~JsonResourceEdit~~
  + removeEditedState(String, String) ResponseEntity~RestResponse~Void~~
}
class ApiUserController {
  + ApiUserController(RepoService, UserService) 
  - Logger log
  - RepoService repoService
  - UserService userService
  + deleteGroup(String, String) ResponseEntity~RestResponse~Void~~
  + getGroups(String, String) ResponseEntity~RestResponse~List~JsonUser~~~
  + addGroup(String, String) ResponseEntity~RestResponse~Void~~
  + addUser(String, String, String) ResponseEntity~RestResponse~Void~~
  + getUsers(String, String) ResponseEntity~RestResponse~List~JsonUser~~~
  + deleteUser(String, String) ResponseEntity~RestResponse~Void~~
}
class AuthController {
  + AuthController(UserAuthenticationManager) 
  - Logger log
  - UserAuthenticationManager authManager
  + logout() ResponseEntity~String~
  + login() ResponseEntity~AuthResponse~
  + login(LoginRequest) ResponseEntity~AuthResponse~
}
class AuthResponse {
  + AuthResponse(String, String) 
  - String error
  - String token
  + token() String
  + error() String
}
class CacheConfig {
  + CacheConfig() 
  + cacheManager(Caffeine~Object, Object~) CacheManager
  + caffeineConfig() Caffeine~Object, Object~
}
class CacheConstants {
  + CacheConstants() 
}
class CacheResourceConstant {
  + CacheResourceConstant() 
  + String REPO_GROUPS
  + String ALL_RESOURCES
  + String REPO_PERMISSIONS
  - List~String~ cacheResourceConstants
  + String REPO_RESOURCES
  + String REPO_USERS
  + String ALL_REPOS
  + String ALL_GROUPS
  + String ALL_USERS
  + getCacheResourceConstants() List~String~
}
class CacheValues {
  + CacheValues() 
  + String CACHE_RESOURCES
}
class ClientException {
  + ClientException() 
  + ClientException(String) 
  + ClientException(String, Throwable) 
  + ClientException(Throwable) 
  + ClientException(String, Throwable, boolean, boolean) 
}
class Constants {
  + Constants() 
}
class ControllerPaths {
  + ControllerPaths() 
  + String API_REPO
  + String AUTH
  + String API_USER
  + String API_RESOURCE
  + String ADMIN
  + String API_PERMISSION
  + String API_ROLE
}
class CoreException {
  + CoreException(String, Throwable, boolean, boolean) 
  + CoreException(String, Throwable) 
  + CoreException() 
  + CoreException(String) 
  + CoreException(Throwable) 
}
class CoreSqlException {
  + CoreSqlException(String) 
  + CoreSqlException(String, Throwable, boolean, boolean) 
  + CoreSqlException() 
  + CoreSqlException(Throwable) 
  + CoreSqlException(String, Throwable) 
}
class CorsData {
  + CorsData() 
  - String path
  - List~String~ allowedHeaders
  - List~String~ allowedMethods
  - String origin
  + getAllowedHeaders() List~String~
  + setAllowedHeaders(List~String~) void
  + setOrigin(String) void
  + setPath(String) void
  + getAllowedMethods() List~String~
  + getPath() String
  + setAllowedMethods(List~String~) void
  + getOrigin() String
}
class CustomErrorController {
  + CustomErrorController(ErrorAttributes) 
  - ErrorAttributes errorAttributes
  - Logger log
  + handleError(HttpServletRequest) ResponseEntity~Map~String, Object~~
}
class CustomUserDetailsService {
  + CustomUserDetailsService(UserAuthenticationManager) 
  - UserAuthenticationManager authManager
  + loadUserByUsername(String) UserDetails
}
class Database~T~ {
  # Database(DatabaseType, T) 
  + DatabaseType MARIA_DB
  + DatabaseType MYSQL
  + DatabaseType POSTGRESQL
  + DatabaseType SQLSERVER
  # DatabaseType databaseType
  + DatabaseType SQLITE
  # T dataSource
  # Logger logger
  + getClassLoader() String
  + getDataSource() T
  + getDriver() String
  + getDatabaseType() DatabaseType
  + sanitize(String) String
}
class DatabaseFunctions {
  - DatabaseFunctions() 
  - Logger log
  - closeConnection(Connection) void
  + initializeTriggers(RepositoryDatabase) void
  + initializeDatabase(RepositoryDatabase) void
  + rebuildFts(RepositoryDatabase) void
}
class DatabaseType {
  + DatabaseType(String, String, String) 
  - String driver
  - String classLoader
  - String name
  + driver() String
  + name() String
  + classLoader() String
}
class DateHelper {
  + DateHelper() 
  + DateTimeFormatter formatter
  + fromDateTime(LocalDateTime) String
  + parseDateTime(String) LocalDateTime
}
class DbHelper {
  - DbHelper() 
  + convertAntPathToSQLLike(String) String
  + validatePath(Path) void
  + validateFileType(Path) void
}
class DbSpeedTest {
  ~ DbSpeedTest() 
  - Logger log
  - RepoProperty properties
  - Faker faker
  ~ testReadWriteSpeed() void
  ~ testWriteSpeed() void
  ~ setUp() void
  - createResource(int) Resource
}
class DbTests {
  ~ DbTests() 
  - RepoProperty properties
  - Faker faker
  ~ setUp() void
  ~ canCreateDatabase() void
  ~ createTestData() void
}
class DocApiApplication {
  + DocApiApplication() 
  + boolean DEV_MODE
  + UserProfile DEV_USER
  + main(String[]) void
}
class FileRepository {
  + FileRepository(RepoProperty) 
  - RepoProperty repoProperties
  - GitRepo gitRepo
  - ScheduledExecutorService executorService
  - RepositoryDatabase dataDB
  - Logger log
  - readData(GitRepo, Path) String
  + getGitRepo() GitRepo
  + initialize() void
  + checkTags(Set~TagId~) void
  - deleteOldResources(List~Path~) void
  + addResourceAndCommit(Resource) void
  - updateMatchingResources(List~Path~, Map~Path, Resource~) int
  - checkFileChanges(Set~Path~) void
  + removeResourceAndCommit(UserId, Path) void
  + getRepoProperties() RepoProperty
  + getDatabase() RepositoryDatabase
  - addNewFiles(List~Path~) void
}
class GitRepo {
  + GitRepo(RepoProperty) 
  - Git git
  - Repository repository
  - Git databaseGit
  - Map~UserId, UserBranch~ currentUserBranches
  - Repository databaseRepository
  - RepoProperty properties
  - Logger log
  - get(Predicate~String~, boolean, GitStage[]) HashSet~Path~
  + remove(Path) void
  - createInitialCommit() void
  + getDatabaseRepoPath() Path
  + push() void
  + getMasterBranchName() String
  + getSingleFile(Predicate~String~, GitStage[]) Optional~Path~
  + getBranch(String) UserBranch
  + commit(String) void
  + getDatabaseRepository() Repository
  + pull() void
  + createBranch(UserId) UserBranch
  - openRepository(Path) void
  - openDatabaseRepository(Path) void
  + getFiles(Predicate~String~, GitStage[]) Set~Path~
  + getRepository() Repository
  + getDatabaseGit() Git
  + getLastCommitDetailsForFile(String) RevCommit
  + getProperties() RepoProperty
  - ensureInitialCommit() void
  - remoteExists(String) boolean
  - handleMissingRepository(Path, boolean) void
  - repoHasCommits() boolean
  + getRepoPath() Path
  + add(Path) void
  + openRepoFromPath(Path) Optional~Repository~
  + createRepoFromPath(Path) void
  + getGit() Git
  - handleMissingDatabaseRepository(Path) void
}
class GitStage {
<<enumeration>>
  - GitStage(Function~Status, Set~String~~) 
  +  ADDED
  +  MODIFIED
  - Function~Status, Set~String~~ getFiles
  +  UNTRACKED
  + valueOf(String) GitStage
  + getFiles(Status) Set~String~
  + values() GitStage[]
}
class Group {
  + Group(GroupId, String, String, LocalDateTime, String, LocalDateTime) 
  + Group(GroupId, String, String, LocalDateTime) 
  + Group(GroupId, String, String, String, String, String) 
  - GroupId id
  - LocalDateTime creationDate
  - String name
  - String modifiedBy
  - String createdBy
  - LocalDateTime lastModified
  - Set~Permission~GroupId~~ permissions
  - Set~UserId~ userIds
  + getName() String
  + getUserIds() Set~UserId~
  + getCreationDate() LocalDateTime
  + getPermissions() Set~Permission~GroupId~~
  + getCreatedBy() String
  + getLastModified() LocalDateTime
  + getId() GroupId
  + getModifiedBy() String
}
class GroupId {
  + GroupId(String) 
  - String id
  + id() String
  + toString() String
  + of(String) GroupId
}
class Identifyable {
<<Interface>>
  + id() String
}
class InvalidPathException {
  + InvalidPathException() 
  + InvalidPathException(String) 
  + InvalidPathException(String, Throwable) 
  + InvalidPathException(String, Throwable, boolean, boolean) 
  + InvalidPathException(Throwable) 
}
class InvalidRepoException {
  + InvalidRepoException(String, Throwable, boolean, boolean) 
  + InvalidRepoException() 
  + InvalidRepoException(String) 
  + InvalidRepoException(String, Throwable) 
  + InvalidRepoException(Throwable) 
}
class InvalidResourceException {
  + InvalidResourceException(String) 
  + InvalidResourceException() 
  + InvalidResourceException(Throwable) 
  + InvalidResourceException(String, Throwable, boolean, boolean) 
  + InvalidResourceException(String, Throwable) 
}
class InvalidTagException {
  + InvalidTagException(String, Throwable) 
  + InvalidTagException() 
  + InvalidTagException(Throwable) 
  + InvalidTagException(String, Throwable, boolean, boolean) 
  + InvalidTagException(String) 
}
class InvalidUserException {
  + InvalidUserException(String, Throwable, boolean, boolean) 
  + InvalidUserException() 
  + InvalidUserException(Throwable) 
  + InvalidUserException(String) 
  + InvalidUserException(String, Throwable) 
}
class JsonFileTree {
  + JsonFileTree(String) 
  - List~JsonResource~ resources
  - String name
  - Map~String, JsonFileTree~ children
  + add(String) JsonFileTree
  + addResource(Resource) void
  + getResources() List~JsonResource~
  + getChildren() Map~String, JsonFileTree~
}
class JsonGroup {
  + JsonGroup(Group) 
  + String name
  + List~String~ users
  + String groupId
  + List~JsonPermission~ permissions
}
class JsonPermission {
  + JsonPermission(Permission~?~) 
  + String path
  + String id
  + boolean isGroup
  + boolean isUser
}
class JsonRepo {
  + JsonRepo(RepoProperty) 
  + String dbStorage
  + String id
  + boolean isReadOnly
  + String path
  + String dbName
  + from(List~RepoProperty~) List~JsonRepo~
}
class JsonResource {
  - JsonResource(Resource) 
  + String createdAt
  + String lastModifiedBy
  + String createdBy
  + Set~String~ tags
  + String repoId
  + String path
  + String lastModifiedAt
  + String data
  + boolean isEditable
  + String category
  + of(List~Resource~) List~JsonResource~
  + of(Resource) JsonResource
}
class JsonResourceEdit {
  + JsonResourceEdit() 
  + boolean isBeingEdited
  + String editingUser
  + String file
}
class JsonRole {
  + JsonRole(String, String) 
  + String id
  + String name
}
class JsonTags {
  + JsonTags(List~Tag~) 
  + Map~String, String~ tags
}
class JsonUser {
  + JsonUser(UserProfile) 
  + List~String~ groups
  + List~JsonRole~ roles
  + List~JsonPermission~ permissions
  + String userId
}
class JwtAuthenticationFilter {
  + JwtAuthenticationFilter(CustomUserDetailsService) 
  - CustomUserDetailsService customUserDetailsService
  - Logger log
  # doFilterInternal(HttpServletRequest, HttpServletResponse, FilterChain) void
}
class JwtUtil {
  + JwtUtil() 
  - SecretKey SECRET_KEY
  - long EXPIRATION_TIME
  + extractUsername(String) String
  + validateToken(String) boolean
  + generateToken(String) String
}
class LoginFailedException {
  + LoginFailedException(String, HttpStatusCode) 
  - HttpStatusCode statusCode
  + getStatusCode() HttpStatusCode
}
class LoginRequest {
  + LoginRequest(String, String) 
  - String password
  - String userId
  + userId() String
  + password() String
}
class Permission~T~ {
  + Permission(T, PermissionType, ResourcePath, RepoId) 
  - PermissionType permission
  - ResourcePath path
  - RepoId repoId
  - T id
  + isUser() boolean
  + getRepoId() RepoId
  + getPath() ResourcePath
  + getId() String
  + setPath(ResourcePath) void
  + isGroup() boolean
  + setPermission(PermissionType) void
  + setRepoId(RepoId) void
}
class PermissionFunctions {
  + PermissionFunctions() 
  - Logger log
  - closeConnection(Connection) void
  + getPermissionsForGroup(RepositoryDatabase, GroupId) List~Permission~GroupId~~
  + getPermissionsForUser(RepositoryDatabase, UserId) Set~Permission~UserId~~
  + getRolesForUser(RepositoryDatabase, UserId) Set~Role~
}
class PermissionRequest {
  + PermissionRequest() 
}
class PermissionType {
<<enumeration>>
  - PermissionType() 
  +  ADMIN
  +  VIEW
  +  EDIT
  +  DENY
  + values() PermissionType[]
  + valueOf(String) PermissionType
}
class RepoControllerTest {
  ~ RepoControllerTest() 
  ~ name() void
}
class RepoId {
  + RepoId(String) 
  + RepoId ALL_REPOS
  - String id
  + equals(Object) boolean
  + hashCode() int
  + filter() Predicate~RepoId~
  + of(String) RepoId
  + id() String
  + isAllRepos() boolean
  + toString() String
}
class RepoProperties {
  + RepoProperties() 
  - List~RepoProperty~ repositories
  + getRepositories() List~RepoProperty~
}
class RepoProperty {
  + RepoProperty() 
  - int idIndex
  - Path path
  - String dbName
  - RepoId id
  - Path dbStorage
  - boolean readOnly
  + isReadOnly() boolean
  + setDbStorage(Path) void
  + setPath(Path) void
  + getId() RepoId
  + setReadOnly(boolean) void
  + setId(RepoId) void
  + getPath() Path
  + getDbStorage() Path
  + setDbName(String) void
  + getDbName() String
}
class RepoService {
  + RepoService(RepoProperties) 
  - Logger log
  - Map~RepoId, FileRepository~ repositories
  - RepoProperties properties
  + getRepo(RepoId) FileRepository
  + validateRepoId(String, boolean) RepoId
  + getRepositories() Map~RepoId, FileRepository~
  + isValidRepo(RepoId) boolean
  + initialize() void
  + validateRepoId(RepoId) void
  + getProperties() List~RepoProperty~
  + validateRepoId(String) RepoId
}
class RepoTests {
  ~ RepoTests() 
  - RepoProperty properties
  + setUp() void
  + exit() void
  ~ canCreateDatabaseInRepo() void
  ~ refuseCreationOfReadonlyRepo() void
  ~ canCreateRepo() void
}
class RepositoryDatabase {
  + RepositoryDatabase(RepoProperty, Path) 
  + RepositoryDatabase(RepoProperty) 
  - Logger log
  - Map~UserId, UserProfile~ userProfiles
  - Map~TagId, Tag~ tagCache
  - AntPathMatcher pathMatcher
  - Map~UserId, List~GroupId~~ userGroups
  - RepoProperty repoProperties
  - Map~GroupId, List~UserId~~ groupUsers
  - Map~GroupId, Group~ groupCache
  - Map~UserId, Path~ currentlyEdited
  - Map~Path, Resource~ resourceCache
  + removeTag(TagId) void
  + getRepoProperties() RepoProperty
  + deleteUser(UserId) boolean
  + removeCurrentlyEdited(UserId) void
  + getUsersFromGroup(GroupId) List~UserId~
  + createUser(UserId, String) boolean
  + groupExists(GroupId) boolean
  + getResources(ResourceRequest) List~Resource~
  + getTagCache() Map~TagId, Tag~
  + batchDelete(List~Path~) void
  + userExists(UserId) boolean
  + tagExists(TagId) boolean
  + getTags(TagId) List~Tag~
  - getDataSource(Path) HikariDataSource
  + tagExists(Tag) boolean
  + insertResource(Resource) void
  + updatePath(Path, Path) void
  + getTags() List~Tag~
  + getUsers(UserId) List~UserProfile~
  + batchUpdate(List~Resource~) void
  + removeCurrentlyEdited(Path) void
  + getRepoId() RepoId
  + removeResource(Path) void
  - initializeCaches() void
  + isUserEditing(UserId) boolean
  + createGroup(GroupId) void
  + getAllUsers() List~UserProfile~
  + batchInsert(List~Resource~) void
  + getGroupsFromUser(UserId) List~GroupId~
  + resourceExists(Path) boolean
  + updateResourceData(ResourceUpdateRequest) Resource
  + initialize() void
  + rebuildFts() void
  + createTag(Tag) void
  + isBeingEdited(Path) UserId
  + setCurrentlyEdited(UserId, Path) void
  + deleteGroup(GroupId) void
}
class Resource {
  + Resource(Path, LocalDateTime, String, LocalDateTime, String, RepoId, Set~TagId~, boolean, String, String) 
  + Resource(Path, String, RepoId, String, Set~TagId~, String) 
  - LocalDateTime modifiedAt
  - String modifiedBy
  - String category
  - String data
  - Set~TagId~ resourceTags
  - LocalDateTime createdAt
  - boolean isEditable
  - RepoId repoId
  - String createdBy
  - Path resourcePath
  + getResourceTags() Set~TagId~
  + setTags(Set~TagId~) Resource
  + hasAnyTag(List~TagId~) boolean
  + equals(Object) boolean
  + setData(String) Resource
  + category() String
  + hasAnyTag(Set~Tag~) boolean
  + hashCode() int
  + getCreatedAt() String
  + copy() Resource
  + getModifiedAt() String
  + data() String
  + repoId() RepoId
  + toString() String
  + createdAt() LocalDateTime
  + modifiedBy() String
  + createdBy() String
  + modifiedAt() LocalDateTime
  + setResourcePath(Path) Resource
  + resourcePath() Path
  + isEditable() boolean
  + setTags(List~TagId~) Resource
}
class ResourceControllerTest {
  ~ ResourceControllerTest() 
  - Logger log
  - RestResponse~Map~?, ?~~ NonResponse
  - TestRestTemplate restTemplate
  + String token
  ~ addResources() void
  ~ removeResources() void
  ~ testGetResources() void
  ~ setUp() void
}
class ResourceException {
  + ResourceException(Throwable) 
  + ResourceException(String, Throwable, boolean, boolean) 
  + ResourceException(String, Throwable) 
  + ResourceException(String) 
  + ResourceException() 
}
class ResourceFunctions {
  - ResourceFunctions() 
  - Logger log
  + updateResource(RepositoryDatabase, ResourceUpdateRequest) Resource
  + addTag(RepositoryDatabase, Tag) void
  + updatePath(RepositoryDatabase, Path, Path) void
  - updateResourceTagsSet(Connection, RepositoryDatabase, Path, List~String~) void
  - addMissingTags(Connection, List~Tag~) void
  + getAllResources(RepositoryDatabase) List~Resource~
  - updateResourceTagsAdd(Connection, RepositoryDatabase, Path, List~String~) void
  - getResource(Connection, RepositoryDatabase, Path) Resource
  - closeConnection(Connection) void
  - updateResourceTagsRemove(Connection, RepositoryDatabase, Path, List~String~) void
  + deleteResource(RepositoryDatabase, Path) void
  + batchInsertResources(RepositoryDatabase, List~Resource~) void
  - fetchTagsForResources(Connection, String) Map~TagId, Tag~
  + batchUpdateResources(RepositoryDatabase, List~Resource~) void
  + removeTag(RepositoryDatabase, TagId) void
  + insertResource(RepositoryDatabase, Resource) void
  + getAllTags(RepositoryDatabase) List~Tag~
  + batchDeleteResources(RepositoryDatabase, List~Path~) void
  - resourceFromResultSet(ResultSet, Set~TagId~, String, RepositoryDatabase) Resource
  + findByContent(RepositoryDatabase, ResourceRequest) Map~Path, String~
  - updateResourceData(Connection, RepositoryDatabase, Path, String) void
}
class ResourcePath {
  + ResourcePath(Path) 
  + ResourcePath(String) 
  - String path
  + getPath() Optional~String~
  + path() String
}
class ResourceRequest {
  + ResourceRequest(String, String, String, String, List~String~, List~String~, boolean, int) 
  + ResourceRequest() 
  + ResourceRequest(String, String, String, String, boolean, int) 
  + int returnLimit
  + String searchTerm
  + String userId
  + boolean withData
  + String repoId
  + List~String~ blacklistTags
  + List~String~ whiteListTags
  + String path
  + setWhiteListTags(List~String~) void
  + getUserId() String
  + getBlacklistTags() List~String~
  + equals(Object) boolean
  + isWithData() boolean
  + getRepoId() String
  + getWhiteListTags() List~String~
  + hashCode() int
  + setPath(String) void
  + getPath() String
  + setBlacklistTags(List~String~) void
  + getSearchTerm() String
  + setRepoId(String) void
  + setWithData(boolean) void
  + setReturnLimit(int) void
  + setSearchTerm(String) void
  + setUserId(String) void
  + getReturnLimit() int
}
class ResourceService {
  + ResourceService(RepoService, UserService) 
  - RepoService repoService
  - UserService userService
  + removeResource(RepoId, Path) boolean
  + setCurrentlyEdited(RepoId, UserId, Path) void
  + isUserEditing(RepoId, UserId) boolean
  + getTags(RepoId, List~TagId~) List~Tag~
  + getTags(RepoId) List~Tag~
  + move(UserId, RepoId, Path, RepoId, Path) Resource
  + updateResource(ResourceUpdateRequest) Resource
  + cleanPath(Path) Path
  - validateResource(RepoId, Path) void
  + tagExists(RepoId, TagId) boolean
  + removeCurrentlyEdited(RepoId, Path) void
  + resourceExists(RepoId, Path) boolean
  + createTag(RepoId, Tag) void
  + removeTag(RepoId, TagId) void
  + getResources(ResourceRequest) List~Resource~
  + insertResource(Resource) void
  + getEditingUser(RepoId, Path) UserId
  + validateTagId(RepoId, TagId) void
  + removeCurrentlyEdited(RepoId, UserId) void
  + cleanPath(String) String
  + isBeingEdited(RepoId, Path) boolean
  + validateTagId(RepoId, String) TagId
}
class ResourceUpdateRequest {
  + ResourceUpdateRequest() 
  + String repoId
  + List~String~ tagsToAdd
  + List~String~ tagsToRemove
  + String category
  + String path
  + String userId
  + List~String~ tagsToSet
  + String data
  + boolean treatNullsAsValues
}
class ResponseTemplates {
  + ResponseTemplates() 
  + errorTemplate(RepoId, String) String
}
class RestResponse~T~ {
  + RestResponse(String, String, T) 
  - T content
  - String message
  - String error
  + success(String, T) RestResponse~T~
  + success(T) RestResponse~T~
  + content() T
  + toResponse(HttpStatusCode) ResponseEntity~RestResponse~T~~
  + message() String
  + error() String
  + toResponse() ResponseEntity~RestResponse~T~~
  + error(String) RestResponse~T~
}
class Role {
  + Role(RoleId, String) 
  - RoleId roleID
  - String roleName
  + roleID() RoleId
  + roleName() String
}
class RoleId {
  + RoleId(String) 
  - String id
  + of(String) RoleId
  + toString() String
  + id() String
}
class SecurityConfig {
  + SecurityConfig(ApiProperties, JwtAuthenticationFilter) 
  - ApiProperties apiProperties
  - JwtAuthenticationFilter jwtAuthenticationFilter
  + authenticationManager(UserDetailsService) AuthenticationManager
  + securityFilterChain(HttpSecurity, AuthenticationManager) SecurityFilterChain
  + corsConfigurer() WebMvcConfigurer
}
class SqliteDatabase~T~ {
  + SqliteDatabase(T) 
  - Pattern pattern
  + close() void
  + getConnection() Connection
}
class Tag {
  + Tag(TagId, String) 
  - TagId tagId
  - String tagName
  + tagId() TagId
  + tagName() String
}
class TagExistsException {
  + TagExistsException(String, Throwable, boolean, boolean) 
  + TagExistsException(String, Throwable) 
  + TagExistsException(String) 
  + TagExistsException() 
  + TagExistsException(Throwable) 
}
class TagId {
  + TagId(String) 
  - String id
  + toString() String
  + id() String
  + of(String) TagId
}
class TestSecurityConfig {
  + TestSecurityConfig() 
  + securityFilterChain(HttpSecurity) SecurityFilterChain
}
class TestUtils {
  + TestUtils() 
  + deleteDirecory(Path) void
}
class ThrowingConsumer~T, E~ {
<<Interface>>
  + accept(T) void
  + andThen(ThrowingConsumer~T, E~) ThrowingConsumer~T, E~
}
class ThrowingFunction~T, R, E~ {
<<Interface>>
  + apply(T) R
  + andThen(ThrowingFunction~R, V, E~) ThrowingFunction~T, V, E~
  + identity() ThrowingFunction~T, T, Throwable~
  + compose(ThrowingFunction~V, T, E~) ThrowingFunction~V, R, E~
}
class UserAuthenticationEntryPoint {
  + UserAuthenticationEntryPoint() 
  - Logger log
  + commence(HttpServletRequest, HttpServletResponse, AuthenticationException) void
}
class UserAuthenticationManager {
  + UserAuthenticationManager() 
  + authenticate(UserId, String) Optional~UserProfile~
  + loadByUserId(UserId) Optional~UserProfile~
  + exists(UserId) boolean
}
class UserBranch {
  + UserBranch(GitRepo, UserId) 
  - String branchName
  - UserId userId
  - GitRepo repo
  - Ref branch
  + removeFile(Path) void
  + addResource(Resource) void
  + addFile(Path) void
  + push(String, String) void
  + updateFileDeleted(Path) void
  + createBranch() void
  + mergeIntoMain() void
  + commit(String) void
  + closeBranch() void
}
class UserControllerTest {
  ~ UserControllerTest() 
  - TestRestTemplate restTemplate
}
class UserFunctions {
  + UserFunctions() 
  - Logger log
  + addUser(RepositoryDatabase, UserId, String, String) boolean
  + addUserToGroup(RepositoryDatabase, UserId, GroupId) void
  - closeConnection(Connection) void
  + deleteUser(RepositoryDatabase, UserId) boolean
  + getAllUserGroups(RepositoryDatabase, Map~UserId, List~GroupId~~, Map~GroupId, List~UserId~~) void
  + getAllGroups(RepositoryDatabase) List~Group~
  + deleteGroup(RepositoryDatabase, GroupId) void
  + getUser(RepositoryDatabase, UserId) UserProfile
  + getAllUsers(RepositoryDatabase) List~UserProfile~
  + getUsersFromGroup(RepositoryDatabase, GroupId) List~UserId~
  + createGroup(RepositoryDatabase, GroupId) boolean
  + getGroupsFromUser(RepositoryDatabase, UserId) List~GroupId~
  + removeUserFromGroup(RepositoryDatabase, UserId, GroupId) void
}
class UserId {
  + UserId(String) 
  - String id
  + UserId ALL_USERS
  + toString() String
  + hashCode() int
  + of(String) UserId
  + isAllUsers() boolean
  + equals(Object) boolean
  + id() String
  + filter() Predicate~UserId~
}
class UserProfile {
  + UserProfile(UserId, String, Set~Permission~UserId~~, Set~Role~) 
  - Set~Role~ roles
  - Gson gson
  - Set~GroupId~ groups
  - Set~Permission~UserId~~ permissionNodes
  - UserId id
  - String passwordHash
  + getRoles() Set~Role~
  + getAllowedResources(List~Resource~) List~Resource~
  + getId() UserId
  + toString() String
  + getGroups() Set~GroupId~
  + getPermissions() Set~Permission~UserId~~
  + getPasswordHash() String
}
class UserRequest {
  + UserRequest() 
}
class UserService {
  + UserService(RepoService) 
  - RepoService repoService
  + groupExists(RepoId, GroupId) boolean
  + deleteGroup(RepoId, GroupId) void
  + validateUserId(RepoId, UserId) void
  + getUsersFromGroup(RepoId, GroupId) List~UserId~
  + deleteUser(RepoId, UserId) boolean
  + userExists(RepoId, UserId) boolean
  + getUsers(RepoId, UserId) List~UserProfile~
  + validateUser(RepoId, String) UserId
  + validateUserId(RepoId, String, boolean) UserId
  + getUsers(String, String) List~UserProfile~
  + createUser(RepoId, UserId, String) boolean
  + getGroupsFromUser(RepoId, UserId) List~GroupId~
  + validateUser(RepoId, UserId) void
  + createGroup(RepoId, GroupId) void
  + validateUserId(RepoId, String) UserId
}
class build {
  + build() 
  + setMetaClass(MetaClass) void
  + setProperty(String, Object) void
  + run() Object
  + getProperty(String) Object
  + getMetaClass() MetaClass
  + main(String[]) void
  + invokeMethod(String, Object) Object
}

ApiProperties "1" *--> "crossOrigin *" CorsData 
ApiRepoController "1" *--> "repoService 1" RepoService 
ApiResourceController "1" *--> "resourceService 1" ResourceService 
ApiUserController "1" *--> "repoService 1" RepoService 
ApiUserController "1" *--> "userService 1" UserService 
AuthController "1" *--> "authManager 1" UserAuthenticationManager 
UserAuthenticationManager  -->  AuthResponse 
CacheConstants  -->  CacheResourceConstant 
Constants  -->  ControllerPaths 
CoreSqlException  -->  CoreException 
ApiProperties  -->  CorsData 
CustomUserDetailsService "1" *--> "authManager 1" UserAuthenticationManager 
Database~T~ "1" *--> "MYSQL 1" DatabaseType 
Database~T~  -->  DatabaseType 
DbSpeedTest "1" *--> "properties 1" RepoProperty 
DbTests "1" *--> "properties 1" RepoProperty 
DocApiApplication "1" *--> "DEV_USER 1" UserProfile 
FileRepository "1" *--> "gitRepo 1" GitRepo 
FileRepository "1" *--> "repoProperties 1" RepoProperty 
FileRepository "1" *--> "dataDB 1" RepositoryDatabase 
GitRepo "1" *--> "properties 1" RepoProperty 
GitRepo "1" *--> "currentUserBranches *" UserBranch 
GitRepo "1" *--> "currentUserBranches *" UserId 
GitRepo  -->  GitStage 
Group "1" *--> "id 1" GroupId 
Group "1" *--> "permissions *" Permission~T~ 
Group "1" *--> "userIds *" UserId 
GroupId  ..>  Identifyable 
InvalidPathException  -->  ClientException 
InvalidRepoException  -->  ClientException 
InvalidResourceException  -->  ClientException 
InvalidTagException  -->  ClientException 
InvalidUserException  -->  ClientException 
JsonFileTree "1" *--> "resources *" JsonResource 
JsonGroup "1" *--> "permissions *" JsonPermission 
JsonUser "1" *--> "permissions *" JsonPermission 
JsonUser "1" *--> "roles *" JsonRole 
JwtAuthenticationFilter "1" *--> "customUserDetailsService 1" CustomUserDetailsService 
UserAuthenticationManager  -->  LoginRequest 
Permission~T~  ..>  Identifyable 
Permission~T~ "1" *--> "permission 1" PermissionType 
Permission~T~ "1" *--> "repoId 1" RepoId 
Permission~T~ "1" *--> "path 1" ResourcePath 
RepoId  ..>  Identifyable 
RepoProperties "1" *--> "repositories *" RepoProperty 
RepoProperty "1" *--> "id 1" RepoId 
RepoService "1" *--> "repositories *" FileRepository 
RepoService "1" *--> "repositories *" RepoId 
RepoService "1" *--> "properties 1" RepoProperties 
RepoTests "1" *--> "properties 1" RepoProperty 
RepositoryDatabase "1" *--> "groupCache *" Group 
RepositoryDatabase "1" *--> "groupUsers *" GroupId 
RepositoryDatabase "1" *--> "repoProperties 1" RepoProperty 
RepositoryDatabase "1" *--> "resourceCache *" Resource 
RepositoryDatabase  -->  SqliteDatabase~T~ 
RepositoryDatabase "1" *--> "tagCache *" Tag 
RepositoryDatabase "1" *--> "tagCache *" TagId 
RepositoryDatabase "1" *--> "userProfiles *" UserId 
RepositoryDatabase "1" *--> "userProfiles *" UserProfile 
Resource "1" *--> "repoId 1" RepoId 
Resource "1" *--> "resourceTags *" TagId 
ResourceControllerTest "1" *--> "NonResponse 1" RestResponse~T~ 
ResourceException  -->  CoreException 
ResourceService "1" *--> "repoService 1" RepoService 
ResourceService "1" *--> "userService 1" UserService 
RoleId  ..>  Identifyable 
SecurityConfig "1" *--> "apiProperties 1" ApiProperties 
SecurityConfig "1" *--> "jwtAuthenticationFilter 1" JwtAuthenticationFilter 
SqliteDatabase~T~  -->  Database~T~ 
TagExistsException  -->  ClientException 
TagId  ..>  Identifyable 
ResourceControllerTest  -->  TestSecurityConfig 
UserBranch "1" *--> "repo 1" GitRepo 
UserBranch "1" *--> "userId 1" UserId 
UserId  ..>  Identifyable 
UserProfile "1" *--> "groups *" GroupId 
UserProfile "1" *--> "permissionNodes *" Permission~T~ 
UserProfile "1" *--> "roles *" Role 
UserProfile "1" *--> "id 1" UserId 
UserService "1" *--> "repoService 1" RepoService 
