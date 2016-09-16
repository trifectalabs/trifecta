module Json.Encode.Internal where


-- Elm Packages
import Json.Encode exposing (..)
import Maybe exposing (withDefault)
import Dict
-- Trifecta Modules
import Util.Types exposing (..)
import Json.Encode.Helper exposing (..)
import UserProfile.Model as UserProfile


userEncoder : UserProfile.User -> Value
userEncoder user =
  object
    [ ("id", string user.id)
    , ("loginInfo", loginInfoEncoder user.loginInfo)
    , ("firstName", withDefault null (Maybe.map string user.firstName))
    , ("lastName", withDefault null (Maybe.map string user.lastName))
    , ("email", withDefault null (Maybe.map string user.email))
    , ("city", withDefault null (Maybe.map string user.city))
    , ("province", withDefault null (Maybe.map string user.province))
    , ("country", withDefault null (Maybe.map string user.country))
    , ("sex", withDefault null (Maybe.map string user.sex))
    , ("dateOfBirth", withDefault null (Maybe.map date user.dateOfBirth))
    , ("avatarURL", withDefault null (Maybe.map string user.avatarURL))
    , ("height", withDefault null (Maybe.map float user.height))
    , ("weight", withDefault null (Maybe.map float user.weight))
    , ("attributes", object (List.map (\(k,v) ->
        (k, userActivityAttributesEncoder v)) (Dict.toList user.attributes)))
    ]


loginInfoEncoder : UserProfile.LoginInfo -> Value
loginInfoEncoder loginInfo =
  object
    [ ("providerID", string loginInfo.providerID)
    , ("providerKey", string loginInfo.providerKey)
    ]


userActivityAttributesEncoder : UserProfile.UserActivityAttributes -> Value
userActivityAttributesEncoder attributes =
  object
    [ ("userID", int attributes.userID)
    , ("activityType", activityTypeEncoder attributes.activityType)
    , ("level", float attributes.level)
    , ("days", int attributes.days)
    , ("activities", int attributes.activities)
    , ("longestDistance", float attributes.longestDistance)
    , ("variance", activityVarianceEncoder attributes.variance)
    , ("activitySpecific", object (List.map (\(k,v) ->
        (k, float v)) (Dict.toList attributes.activitySpecific)))
    , ("createdAt", dateTime attributes.createdAt)
    , ("archived", bool attributes.archived)
    ]


activityVarianceEncoder : UserProfile.ActivityVariance -> Value
activityVarianceEncoder variance =
  object
    [ ("fractionShort", float variance.fractionShort)
    , ("fractionAverage", float variance.fractionAverage)
    , ("fractionLong", float variance.fractionLong)
    ]


activityTypeEncoder : ActivityType -> Value
activityTypeEncoder type' =
  string (toString type')
