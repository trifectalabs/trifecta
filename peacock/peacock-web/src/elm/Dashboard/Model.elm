module Dashboard.Model where


-- Elm Packages
import Date exposing (Date, fromTime)
-- Trifecta Modules
import Util.Types exposing (..)


type alias Model =
  { trainingPlans : List TrainingPlan
  }


type alias TrainingPlan =
  { id : Int
  , userID : String
  , activityType : ActivityType
  , startDate : Date
  , endDate : Date
  , createdAt : Date
  , archived : Bool
  , activities : List TrainingPlanActivity
  }


type alias TrainingPlanActivity =
  { id : Int
  , userDefined : Bool
  , distance : Float
  , time : Float
  , elevation : Float
  , activityID : Maybe Int
  , calendarEventID : Maybe Int
  }


fakeUser =
  { id = 1
  , name = "Chris Poenaru"
  , email = "cpoenaru@uwaterloo.ca"
  , displayPicture = "https://dgalywyr863hv.cloudfront.net/pictures/" ++
    "athletes/1271201/913317/9/large.jpg"
  , auth = "AUTH STRING YO"
  }


fakeTrainingPlan =
  { id = 1
  , userID = 1
  , activityType = Ride
  , startDate = fromTime 0
  , endDate = fromTime 0
  , createdAt = fromTime 0
  , archived = False
  , activities = fakeActivities
  }


fakeActivities =
  [ fakeActivity
  , fakeActivity
  , fakeActivity
  ]


fakeActivity =
  { id = 1
  , userDefined = False
  , distance = 30.0
  , time = 60.0
  , elevation = 200.0
  , activityID = Nothing
  , calendarEventID = Nothing
  }
