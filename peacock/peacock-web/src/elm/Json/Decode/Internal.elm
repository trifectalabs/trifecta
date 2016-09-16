module Json.Decode.Internal where


-- Elm Packages
import Date exposing (Date, fromTime)
import Json.Decode exposing (..)
import Json.Decode.Extra as JsonEx
import Json.Decode.Helper exposing (dateDecoder, apply)
-- Trifecta Modules
import Dashboard.Model as Dashboard
import UserProfile.Model as UserProfile
import Util.Types exposing (..)


trainingPlanDecoder : Decoder Dashboard.TrainingPlan
trainingPlanDecoder =
  object8 Dashboard.TrainingPlan
    ("id" := int)
    ("userID" := string)
    ("activityType" := activityTypeDecoder)
    ("startDate" := JsonEx.date)
    ("endDate" := JsonEx.date)
    ("createdAt" := dateDecoder)
    ("archived" := bool)
    ("activities" := list trainingPlanActivityDecoder)


trainingPlanActivityDecoder : Decoder Dashboard.TrainingPlanActivity
trainingPlanActivityDecoder =
  object7 Dashboard.TrainingPlanActivity
    ("id" := int)
    ("userDefined" := bool)
    ("distance" := float)
    ("time" := float)
    ("elevation" := float)
    (maybe ("activityID" := int))
    (maybe ("calendarEventID" := int))


userDecoder : Decoder UserProfile.User
userDecoder =
  map UserProfile.User
    ("id" := string) `apply`
    ("loginInfo" := loginInfoDecoder) `apply`
    (maybe ("firstName" := string)) `apply`
    (maybe ("lastName" := string)) `apply`
    (maybe ("email" := string)) `apply`
    (maybe ("city" := string)) `apply`
    (maybe ("province" := string)) `apply`
    (maybe ("country" := string)) `apply`
    (maybe ("sex" := string)) `apply`
    (maybe ("dateOfBirth" := dateDecoder)) `apply`
    (maybe ("avatarURL" := string)) `apply`
    (maybe ("height" := float)) `apply`
    (maybe ("weight" := float)) `apply`
    ("attributes" := dict userActivityAttributesDecoder)


loginInfoDecoder : Decoder UserProfile.LoginInfo
loginInfoDecoder =
  object2 UserProfile.LoginInfo
    ("providerID" := string)
    ("providerKey" := string)


userActivityAttributesDecoder : Decoder UserProfile.UserActivityAttributes
userActivityAttributesDecoder =
  map UserProfile.UserActivityAttributes
    ("userID" := int) `apply`
    ("activityType" := activityTypeDecoder) `apply`
    ("level" := float) `apply`
    ("days" := int) `apply`
    ("activities" := int) `apply`
    ("longestDistance" := float) `apply`
    ("variance" := activityVarianceDecoder) `apply`
    ("activitySpecific" := dict float) `apply`
    ("createdAt" := JsonEx.date) `apply`
    ("archived" := bool)


activityVarianceDecoder : Decoder UserProfile.ActivityVariance
activityVarianceDecoder =
  object3 UserProfile.ActivityVariance
    ("fractionShort" := float)
    ("fractionAverage" := float)
    ("fractionLong" := float)


activityTypeDecoder : Decoder ActivityType
activityTypeDecoder = customDecoder string (\s ->
  case s of
    "Ride" -> Result.Ok Ride
    "Run" -> Result.Ok Run
    "Swim" -> Result.Ok Swim
    _ -> Result.Err ("invalid ActivityType " ++ s))
