module UserProfile.Model where


-- Elm Packages
import Date exposing (Date, fromTime)
import Dict exposing (Dict)
-- Trifecta Modules
import Util.Types exposing (..)


type alias Model =
  { currentUser : Maybe User
  , profileForm : Maybe User
  , editing : UserEditing
  }


type alias User =
  { id : String
  , loginInfo : LoginInfo
  , firstName : Maybe String
  , lastName : Maybe String
  , email : Maybe String
  , city : Maybe String
  , province : Maybe String
  , country : Maybe String
  , sex : Maybe String
  , dateOfBirth : Maybe Date
  , avatarURL : Maybe String
  , height : Maybe Float
  , weight : Maybe Float
  , attributes : Dict String UserActivityAttributes
  }


type alias UserEditing =
  { name : Bool
  , email : Bool
  , location : Bool
  , sex : Bool
  , birthday : Bool
  , height : Bool
  , weight : Bool
  , attributes : Dict String Bool
  }


type alias UserActivityAttributes =
  { userID : Int
  , activityType : ActivityType
  , level : Float
  , days : Int
  , activities : Int
  , longestDistance : Float
  , variance : ActivityVariance
  , activitySpecific : Dict String Float
  , createdAt : Date
  , archived : Bool
  }


type alias ActivityVariance =
  { fractionShort : Float
  , fractionAverage : Float
  , fractionLong : Float
  }


type alias LoginInfo =
  { providerID : String
  , providerKey : String
  }


defaultUser : User
defaultUser =
  { id = "default"
  , loginInfo = LoginInfo "default" "default"
  , firstName = Nothing
  , lastName = Nothing
  , email = Nothing
  , city = Nothing
  , province = Nothing
  , country = Nothing
  , sex = Nothing
  , dateOfBirth = Nothing
  , avatarURL = Nothing
  , height = Nothing
  , weight = Nothing
  , attributes = Dict.empty
  }


defaultEditing : UserEditing
defaultEditing =
  { name = False
  , email = False
  , location = False
  , sex = False
  , birthday = False
  , height = False
  , weight = False
  , attributes =
      (Dict.fromList
        [ ("Ride", False)
        , ("Run", False)
        , ("Swim", False)
        ])
  }


defaultAttributes : Dict String UserActivityAttributes
defaultAttributes =
  Dict.fromList
    [ ("Ride", defaultRideAttributes)
    , ("Run", defaultRunAttributes)
    , ("Swim", defaultSwimAttributes)
    ]


defaultRideAttributes : UserActivityAttributes
defaultRideAttributes =
  UserActivityAttributes 1 Ride 1 14 8 50 (ActivityVariance 0.5 0.5 0)
    (Dict.fromList [("BikeRollingResistance", 0.004), ("BikeDrag", 1.0)])
    (fromTime 0) False


defaultRunAttributes : UserActivityAttributes
defaultRunAttributes =
  UserActivityAttributes 1 Run 1 14 8 10 (ActivityVariance 0.5 0.5 0) Dict.empty
    (fromTime 0) False


defaultSwimAttributes : UserActivityAttributes
defaultSwimAttributes =
  UserActivityAttributes 1 Swim 1 14 8 1 (ActivityVariance 0.5 0.5 0) Dict.empty
    (fromTime 0) False
