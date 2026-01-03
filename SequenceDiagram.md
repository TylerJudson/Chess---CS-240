actor Client
participant Server
participant Handler
participant Service
participant DataAccess
database db

entryspacing 0.9
group#43829c #lightblue Registration
Client -> Server: [POST] /user\n{"username":" ", "password":" ", "email":" "}
Server -> Handler: {"username":" ", "password":" ", "email":" "}
Handler -> Service: register(RegisterRequest)
Service -> DataAccess: getUser(username)
DataAccess -> db:Find UserData by username
break User with username already exists
DataAccess --> Service: UserData
Service --> Server: AlreadyTakenException
Server --> Client: 403\n{"message": "Error: username already taken"}
end
DataAccess --> Service: null
Service -> DataAccess:createUser(userData)
DataAccess -> db:Add UserData
Service -> DataAccess:createAuth(authData)
DataAccess -> db:Add AuthData
Service --> Handler: RegisterResult
Handler --> Server: {"username" : " ", "authToken" : " "}
Server --> Client: 200\n{"username" : " ", "authToken" : " "}
end

group#orange #FCEDCA Login
Client -> Server: [POST] /session\n{username, password}
Server -> Handler: {username, password}
Handler -> Service: loginUser(username, password)
Service -> DataAccess: getUser(username)
DataAccess -> db: Find UserData by username

break invalid username
DataAccess --> Service: null
Service --> Server: UserDoesn'tExist
Server -> Client: 401\n{message: Unauthorized}
end

DataAccess --> Service: UserData
Service -> Service: verifyPassword(password, hash)
    
break invalid password
Service --> Server: Unauthorized
Server --> Client: 401\n{message: Unauthorized}
end

Service -> DataAccess: createAuth(authData)
DataAccess -> db: Add AuthData


Service --> Handler: LoginResult
Handler --> Server: {username:" ", authToken:" "}
Server --> Client: 200\n{username:" ", authToken:" "} 

end

group#green #lightgreen Logout
Client -> Server: [DELETE] /session\nauthToken
Server -> Handler: authToken
Handler -> Service: LogoutUser(authToken)
Service -> DataAccess: getAuthData(authToken)
DataAccess -> db: Find AuthData

break AuthToken not found
DataAccess --> Service: null
Service --> Server: Unauthorized
Server --> Client: 401\n{message: Unauthorized}
end

DataAccess --> Service: AuthData

Service -> DataAccess: removeAuthData(AuthData)
DataAccess -> db: Delete AuthData

Service --> Handler: LogoutResult
Handler --> Server: 200\n{}
Server --> Client: 200\n{}
end

group#red #pink List Games
Client -> Server: [GET] /game\nauthToken
Server -> Handler: authToken
Handler -> Service: listGames(authToken)
Service -> DataAccess: getAuthData(authToken)
DataAccess -> db: Find AuthData

break AuthToken not found
DataAccess --> Service: null
Service --> Server: Unauthorized
Server --> Client: 401\n{message: Unauthorized}
end

DataAccess --> Service: AuthData

Service -> DataAccess: getGames()
DataAccess -> db: Find all games
DataAccess --> Service: Games[]
Service --> Handler: Games[]
Handler --> Server: 200\n{games: [...]}
Server --> Client: 200\n{games: [...]}

end

group#d790e0 #E3CCE6 Create Game 
Client -> Server: [POST] /game\nauthToken, {gameName}
Server -> Handler: authToken, {gameName}
Handler -> Service: createGame(authToken, gameName)

Service -> Service: checkGameName()

break GameName is nil or ""
Service --> Handler: BadRequest
Handler --> Server: 400\n{message: "Bad Request"}
Server --> Client: 400\n{message: "Bad Request"}
end

Service -> DataAccess: getAuthData(authToken)
DataAccess -> db: Find AuthData

break AuthToken not found
DataAccess --> Service: null
Service --> Server: Unauthorized
Server --> Client: 401\n{message: Unauthorized}
end

DataAccess --> Service: AuthData
Service -> DataAccess: createGame(gameName)
DataAccess -> db: Create Game with gameName
DataAccess --> Service: gameId
Service -> Handler: gameId
Handler -> Server: 200\n{gameId: " "}
Server -> Client: 200\n{gameId: " "}

end

group#yellow #lightyellow Join Game #black
Client -> Server: [PUT] /game\nauthToken, {playerColor, gameId}
Server -> Handler: authToken, {playerColor, gameId}
Handler -> Service: joinGame(authToken, playerColor, gameId)

Service -> DataAccess: getAuthData(authToken)
DataAccess -> db: Find AuthData

break AuthToken not found
DataAccess --> Service: null
Service --> Server: Unauthorized
Server --> Client: 401\n{message: Unauthorized}
end

DataAccess --> Service: AuthData
Service -> DataAccess: getGame(gameId)
DataAccess -> db: Find Game

break GameId doesn't exist
DataAccess --> Service: null
Service --> Handler: GameDoesn'tExist
Handler --> Server: 400\n{message: "Bad Request"}
Server --> Client: 400\n{message: "Bad Request"}
end

DataAccess --> Service: Game
Service -> Service: validateColor(playerColor)

break Player Color is invalid
Service --> Handler: InvalidPlayer
Handler --> Server: 400\n{message: "Bad Request"}
Server --> Client: 400\n{message: "Bad Request"}
end

break Player Color is already taken
Service --> Handler: AlreadyTaken
Handler --> Server: 403\n{message: "Already Taken"}
Server --> Client: 403\n{message: "Already Taken"}
end

Service -> DataAccess: GetUser(AuthData)
DataAccess -> db: Find User
DataAccess --> Service: User
Service -> DataAccess: AddUserToGame(User, playerColor)
DataAccess -> db: Add User to Game with PlayerColor
Service --> Handler: JoinResult
Handler --> Server: 200\n{}
Server --> Client: 200\n{}
end

group#gray #lightgray Clear application 
Client -> Server: [DELETE] /db
Server -> Handler: deleteDB()
Handler -> Service: deleteDB()
Service -> DataAccess: deleteAllData()
DataAccess -> db: Delete all data
Service --> Handler: DeleteResult
Handler --> Server: 200\n{}
Server --> Client: 200\n{}
end