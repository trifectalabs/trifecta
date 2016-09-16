module SignIn.Actions where


type Action
  = SetFirstName String
  | SetLastName String
  | SetEmail String
  | SetPassword String
