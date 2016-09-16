module UserProfile.View where


-- Elm Packages
import Signal exposing (Address)
import Html exposing (Html, div, span, text, img, nav, a, button, input)
import Html.Attributes as Attr exposing (class, type', placeholder, value, step)
import Html.Events exposing (onClick, on, targetValue)
import Maybe exposing (withDefault)
import Date exposing (Date, fromTime, fromString)
import Date.Format as DateFormatter
import Number.Format as NumberFormatter
import Dict
import String exposing (join)
import List exposing (append)
-- Trifecta Modules
import Main.Actions as Main
import Main.Model as M
import Main.View exposing (appView)
import Util.Types exposing (..)
import Dashboard.Actions as Dashboard
import UserProfile.Actions exposing (..)
import UserProfile.Model exposing (..)


profileAddControl : Address Action -> ActivityType -> Html
profileAddControl address activityType =
  div [ class "profile-controls" ]
    [ span
        [ class "link-icon glyphicon glyphicon-plus"
        , onClick address (SetAttribute activityType (SetAttr AddAttribute))
        ] []
    ]


profileEditControl : Address Action -> (EditAction -> Action) ->
  Maybe ActivityType -> Bool -> Html
profileEditControl address action activityType removeable =
  let
    rightControl = case (activityType, removeable) of
      (Just type', True) ->
        span
          [ class "link-icon glyphicon glyphicon-minus"
          , onClick address
              (SetAttribute type' (SetAttr RemoveAttribute))
          ] []
      _ ->
        span [] []
  in
    div [ class "profile-controls" ]
      [ span
          [ class "link-icon glyphicon glyphicon-pencil"
          , onClick address (action EditValue)
          ] []
      , rightControl
      ]


profileSaveControl : Address Action -> (EditAction -> Action) -> Html
profileSaveControl address action =
  div [ class "profile-controls" ]
    [ span
        [ class "link-icon glyphicon glyphicon-remove"
        , onClick address (action CancelEdit)
        ] []
    , span
        [ class "link-icon glyphicon glyphicon-ok"
        , onClick address (action SaveValue)
        ] []
    ]

profileView : Address Dashboard.Action -> Address Action ->
  String -> String -> Int -> M.Model -> Html
profileView dashboardAddress address _ hash length model =
  let
    headerText = "User Profile"
    subheaderText = "You can finish setting up your profile below. " ++
      "In order to build a training plan for an activity, your height, " ++
      "weight, and that activity's attributes are required."
    user = withDefault defaultUser model.userProfile.currentUser
    subheader =
      case (user.height, user.weight, (Dict.toList user.attributes)) of
        (Just h, Just w, attr::tail) -> span [] []
        _ -> span [ class "dashboard-subheader" ] [ text subheaderText ]
    userView = case model.userProfile.currentUser of
      Nothing -> span [] []
      Just u -> userDisplayForm address model.userProfile
  in
    appView dashboardAddress model
      [ div [ class "dashboard" ]
          [ span [ class "dashboard-header" ] [ text headerText ]
          , subheader
          , userView
          ]
      ]


userDisplayForm : Address Action -> Model -> Html
userDisplayForm address userProfile =
  let
    user = withDefault defaultUser userProfile.currentUser
    form = withDefault defaultUser userProfile.profileForm
    editing = userProfile.editing
    def = "Not Specified"
    name = join " "
      [(withDefault def user.firstName), (withDefault def user.lastName)]
    email = withDefault "" user.email
    location = case (user.city, user.province, user.country) of
      (Nothing, Nothing, Nothing) -> def
      (Just city, Nothing, Nothing) -> city
      (Nothing, Just province, Nothing) -> province
      (Nothing, Nothing, Just country) -> country
      (Just city, Just province, Nothing) -> join ", " [city, province]
      (Just city, Nothing, Just country) -> join ", " [city, country]
      (Nothing, Just province, Just country) -> join " " [province, country]
      (Just city, Just province, Just country) -> join " "
        [(join ", " [city, province]), country]
    sex = withDefault def user.sex
    birthday = case user.dateOfBirth of
      Nothing -> def
      Just date -> (DateFormatter.format "%b %e, %Y" date)
    height = case user.height of
      Nothing -> def
      Just h -> toString h
    weight = case user.weight of
      Nothing -> def
      Just w -> toString w
    nameElement = case editing.name of
      False ->
        div [ class "profile-section" ]
          [ div [ class "profile-element" ]
              [ span [ class "profile-label" ] [ text "Name" ]
              , span [ class "profile-value" ] [ text name ]
              , profileEditControl address SetFirstName Nothing False
              ]
          ]
      True ->
        div [ class "profile-section" ]
          [ div [ class "profile-element" ]
              [ span [ class "profile-label"] [ text "Name" ]
              , span [ class "profile-input-group" ]
                  [ input
                      [ type' "text"
                      , value (withDefault "" form.firstName)
                      , placeholder "First Name"
                      , on "input" targetValue
                        (\str -> Signal.message address (SetFirstName (SetString str)))
                      ] []
                  , input
                      [ type' "text"
                      , value (withDefault "" form.lastName)
                      , placeholder "Last Name"
                      , on "input" targetValue
                        (\str -> Signal.message address (SetLastName (SetString str)))
                      ] []
                  ]
              , profileSaveControl address SetFirstName
              ]
          ]
    emailElement = case editing.email of
      False ->
        div [ class "profile-section" ]
          [ div [ class "profile-element" ]
              [ span [ class "profile-label" ] [ text "Email" ]
              , span [ class "profile-value" ] [ text email ]
              , profileEditControl address SetEmail Nothing False
              ]
          ]
      True ->
        div [ class "profile-section" ]
          [ div [ class "profile-element" ]
              [ span [ class "profile-label" ] [ text "Email" ]
              , span [ class "profile-input-group" ]
                  [ input
                      [ type' "text"
                      , value (withDefault "" form.email)
                      , placeholder "Email"
                      , on "input" targetValue
                        (\str -> Signal.message address (SetEmail (SetString str)))
                      ] []
                  ]
              , profileSaveControl address SetEmail
              ]
          ]
    locationElement = case editing.location of
      False ->
        div [ class "profile-section" ]
          [ div [ class "profile-element" ]
              [ span [ class "profile-label" ] [ text "Location" ]
              , span [ class "profile-value" ] [ text location ]
              , profileEditControl address SetCity Nothing False
              ]
          ]
      True ->
        div [ class "profile-section" ]
          [ div [ class "profile-element" ]
              [ span [ class "profile-label" ] [ text "Location" ]
              , span [ class "profile-input-group" ]
                  [ input
                      [ type' "text"
                      , value (withDefault "" form.city)
                      , placeholder "City"
                      , on "input" targetValue
                        (\str -> Signal.message address (SetCity (SetString str)))
                      ] []
                  , input
                      [ type' "text"
                      , value (withDefault "" form.province)
                      , placeholder "Province or State"
                      , on "input" targetValue
                        (\str -> Signal.message address (SetProvince (SetString str)))
                      ] []
                  , input
                      [ type' "text"
                      , value (withDefault "" form.country)
                      , placeholder "Country"
                      , on "input" targetValue
                        (\str -> Signal.message address (SetCountry (SetString str)))
                      ] []
                  ]
              , profileSaveControl address SetCity
              ]
          ]
    sexElement = case editing.sex of
      False ->
        div [ class "profile-section" ]
          [ div [ class "profile-element" ]
              [ span [ class "profile-label" ] [ text "Sex" ]
              , span [ class "profile-value" ] [ text sex ]
              , profileEditControl address SetSex Nothing False
              ]
          ]
      True ->
        div [ class "profile-section" ]
          [ div [ class "profile-element" ]
              [ span [ class "profile-label" ] [ text "Sex" ]
              , span [ class "profile-input-group" ]
                  [ input
                      [ type' "text"
                      , value (withDefault "" form.sex)
                      , placeholder "Sex"
                      , on "input" targetValue
                        (\str -> Signal.message address (SetSex (SetString str)))
                      ] []
                  ]
              , profileSaveControl address SetSex
              ]
          ]
    birthdayElement = case editing.birthday of
      False ->
        div [ class "profile-section" ]
          [ div [ class "profile-element" ]
              [ span [ class "profile-label" ] [ text "Birthday" ]
              , span [ class "profile-value" ] [ text birthday ]
              , profileEditControl address SetBirthday Nothing False
              ]
          ]
      True ->
        div [ class "profile-section" ]
          [ div [ class "profile-element" ]
              [ span [ class "profile-label" ] [ text "Birthday" ]
              , span [ class "profile-input-group" ]
                  [ input
                      [ type' "date"
                      , value (withDefault "" (Maybe.map
                          (DateFormatter.format "%Y-%m-%d") form.dateOfBirth))
                      , on "input" targetValue (\str ->
                          case (fromString str) of
                            Ok bday ->
                              Signal.message address (SetBirthday (SetDate bday))
                            Err _ ->
                              Signal.message address NoOp)
                      ] []
                  ]
              , profileSaveControl address SetBirthday
              ]
          ]
    heightElement = case editing.height of
      False ->
        div [ class "profile-section" ]
          [ div [ class "profile-element" ]
              [ span [ class "profile-label" ] [ text "Height (cm)" ]
              , span [ class "profile-value" ] [ text height ]
              , profileEditControl address SetHeight Nothing False
              ]
          ]
      True ->
        div [ class "profile-section" ]
          [ div [ class "profile-element" ]
              [ span [ class "profile-label" ] [ text "Height (cm)" ]
              , span [ class "profile-input-group" ]
                  [ input
                      [ type' "number"
                      , value (withDefault "" (Maybe.map toString form.height))
                      , step "0.1"
                      , Attr.min "0"
                      , on "input" targetValue (\str ->
                          case (String.toFloat str) of
                            Ok h -> Signal.message address (SetHeight (SetFloat h))
                            Err _ -> Signal.message address NoOp)
                      ] []
                  ]
              , profileSaveControl address SetHeight
              ]
          ]
    weightElement = case editing.weight of
      False ->
        div [ class "profile-section" ]
          [ div [ class "profile-element" ]
              [ span [ class "profile-label" ] [ text "Weight (kg)" ]
              , span [ class "profile-value" ] [ text weight ]
              , profileEditControl address SetWeight Nothing False
              ]
          ]
      True ->
        div [ class "profile-section" ]
          [ div [ class "profile-element" ]
              [ span [ class "profile-label" ] [ text "Weight (kg)" ]
              , span [ class "profile-input-group" ]
                  [ input
                      [ type' "number"
                      , value (withDefault "" (Maybe.map toString form.weight))
                      , step "0.1"
                      , Attr.min "0"
                      , on "input" targetValue (\str ->
                          case (String.toFloat str) of
                            Ok w -> Signal.message address (SetWeight (SetFloat w))
                            Err _ -> Signal.message address NoOp)
                      ] []
                  ]
              , profileSaveControl address SetWeight
              ]
          ]
    rideAttrElement =
      case (withDefault False (Dict.get "Ride" editing.attributes)) of
        False -> userAttributesView address Ride "Cycling Attributes"
          (Dict.get "Ride" user.attributes) False
        True -> userAttributesView address Ride "Cycling Attributes"
          (Dict.get "Ride" form.attributes) True
    runAttrElement =
      case (withDefault False (Dict.get "Run" editing.attributes)) of
        False -> userAttributesView address Run "Running Attributes"
          (Dict.get "Run" user.attributes) False
        True -> userAttributesView address Run "Running Attributes"
          (Dict.get "Run" form.attributes) True
    swimAttrElement =
      case (withDefault False (Dict.get "Swim" editing.attributes)) of
        False -> userAttributesView address Swim "Swimming Attributes"
          (Dict.get "Swim" user.attributes) False
        True -> userAttributesView address Swim "Swimming Attributes"
          (Dict.get "Swim" form.attributes) True
  in
    div [ class "profile" ]
      [ nameElement
      , emailElement
      , locationElement
      , sexElement
      , birthdayElement
      , heightElement
      , weightElement
      , rideAttrElement
      , runAttrElement
      , swimAttrElement
      ]


userAttributesView : Address Action -> ActivityType -> String ->
  Maybe UserActivityAttributes -> Bool -> Html
userAttributesView address activityType label perhapsAttributes editing =
  case (perhapsAttributes, editing) of
    (Nothing, False) ->
      div [ class "profile-section" ]
        [ div [ class "profile-element" ]
            [ span [ class "profile-label" ] [ text label ]
            , span [ class "profile-value" ] [ text "Not Specified" ]
            , profileAddControl address activityType
            ]
        ]
    _ ->
      let
        attributes = case perhapsAttributes of
          Nothing -> case activityType of
            Ride -> defaultRideAttributes
            Run -> defaultRunAttributes
            Swim -> defaultSwimAttributes
          Just a -> a
        daysElement = case editing of
          False ->
            div [ class "profile-element" ]
              [ span [ class "profile-label" ] [ text "Days" ]
              , span [ class "profile-value" ]
                  [ text (toString attributes.days) ]
              ]
          True ->
            div [ class "profile-element" ]
              [ span [ class "profile-label" ] [ text "Days" ]
              , span [ class "profile-input-group" ]
                  [ input
                      [ type' "number"
                      , value (toString attributes.days)
                      , Attr.min "0"
                      , on "input" targetValue (\str ->
                          case (String.toInt str) of
                            Ok d -> Signal.message address (SetAttribute
                              activityType (SetAttr (SetDays d)))
                            Err _ -> Signal.message address NoOp)
                      ] []
                  ]
              ]
        activitiesElement = case editing of
          False ->
            div [ class "profile-element" ]
              [ span [ class "profile-label" ] [ text "Activities" ]
              , span [ class "profile-value" ]
                  [ text (toString attributes.activities) ]
              ]
          True ->
            div [ class "profile-element" ]
              [ span [ class "profile-label" ] [ text "Activities" ]
              , span [ class "profile-input-group" ]
                  [ input
                      [ type' "number"
                      , value (toString attributes.activities)
                      , Attr.min "0"
                      , on "input" targetValue (\str ->
                          case (String.toInt str) of
                            Ok a -> Signal.message address (SetAttribute
                              activityType (SetAttr (SetActivities a)))
                            Err _ -> Signal.message address NoOp)
                      ] []
                  ]
              ]
        longestDistanceElement = case editing of
          False ->
            div [ class "profile-element" ]
              [ span [ class "profile-label" ] [ text "Longest Distance" ]
              , span [ class "profile-value" ]
                  [ text (toString attributes.longestDistance) ]
              ]
          True ->
            div [ class "profile-element" ]
              [ span [ class "profile-label" ] [ text "Longest Distance" ]
              , span [ class "profile-input-group" ]
                  [ input
                      [ type' "number"
                      , value (toString attributes.longestDistance)
                      , Attr.min "0"
                      , step "0.1"
                      , on "input" targetValue (\str ->
                          case (String.toFloat str) of
                            Ok d -> Signal.message address (SetAttribute
                              activityType (SetAttr (SetLongest d)))
                            Err _ -> Signal.message address NoOp)
                      ] []
                  ]
              ]
        actSpecificAttr = List.map
          (activitySpecificView address activityType editing)
          (Dict.toList attributes.activitySpecific)
        actSpecificGroup = case actSpecificAttr of
          [] ->
            span [] []
          _ ->
            div [ class "profile-group" ]
              ((span [ class "profile-subheader" ] [ text "Activity Specific" ])
                :: actSpecificAttr)
        basicAttr =
          [ span [ class "profile-header" ]
              [ span [ class "header-text" ] [ text label ]
              , controls
              ]
          , daysElement
          , activitiesElement
          , longestDistanceElement
          , activityVarianceView
              address activityType attributes.variance editing
          , actSpecificGroup
          ]
        controls = case editing of
          False -> profileEditControl address
            (SetAttribute activityType) (Just activityType) True
          True -> profileSaveControl address (SetAttribute activityType)
      in
        div [ class "profile-section" ] basicAttr


activityVarianceView : Address Action -> ActivityType ->
  ActivityVariance -> Bool -> Html
activityVarianceView address activityType variance editing =
  let
    shortElement = case editing of
      False ->
        div [ class "profile-element" ]
          [ span [ class "profile-label" ] [ text "Fraction Short" ]
          , span [ class "profile-value" ]
              [ text (toString variance.fractionShort) ]
          ]
      True ->
        div [ class "profile-element" ]
          [ span [ class "profile-label" ] [ text "Fraction Short" ]
          , span [ class "profile-input-group" ]
              [ input
                  [ type' "number"
                  , value (toString variance.fractionShort)
                  , Attr.min "0"
                  , Attr.max "1"
                  , step "0.1"
                  , on "input" targetValue (\str ->
                      case (String.toFloat str) of
                        Ok f -> Signal.message address (SetAttribute
                          activityType (SetAttr (SetShortFraction f)))
                        Err _ -> Signal.message address NoOp)
                  ] []
              ]
          ]
    averageElement = case editing of
      False ->
        div [ class "profile-element" ]
          [ span [ class "profile-label" ] [ text "Fraction Average" ]
          , span [ class "profile-value" ]
              [ text (toString variance.fractionAverage) ]
          ]
      True ->
        div [ class "profile-element" ]
          [ span [ class "profile-label" ] [ text "Fraction Average" ]
          , span [ class "profile-input-group" ]
              [ input
                  [ type' "number"
                  , value (toString variance.fractionAverage)
                  , Attr.min "0"
                  , Attr.max "1"
                  , step "0.1"
                  , on "input" targetValue (\str ->
                      case (String.toFloat str) of
                        Ok f -> Signal.message address (SetAttribute
                          activityType (SetAttr (SetAverageFraction f)))
                        Err _ -> Signal.message address NoOp)
                  ] []
              ]
          ]
    longElement = case editing of
      False ->
        div [ class "profile-element" ]
          [ span [ class "profile-label" ] [ text "Fraction Long" ]
          , span [ class "profile-value" ]
              [ text (toString variance.fractionLong) ]
          ]
      True ->
        div [ class "profile-element" ]
          [ span [ class "profile-label" ] [ text "Fraction Long" ]
          , span [ class "profile-input-group" ]
              [ input
                  [ type' "number"
                  , value (toString variance.fractionLong)
                  , Attr.min "0"
                  , Attr.max "1"
                  , step "0.1"
                  , on "input" targetValue (\str ->
                      case (String.toFloat str) of
                        Ok f -> Signal.message address (SetAttribute
                          activityType (SetAttr (SetLongFraction f)))
                        Err _ -> Signal.message address NoOp)
                  ] []
              ]
          ]
  in
    div [ class "profile-group" ]
      [ span [ class "profile-subheader" ] [ text "Activity Variance" ]
      , shortElement
      , averageElement
      , longElement
      ]


activitySpecificView : Address Action -> ActivityType ->
  Bool -> (String, Float) -> Html
activitySpecificView address activityType editing (key, val) =
  case editing of
    False ->
      div [ class "profile-element" ]
        [ span [ class "profile-label" ] [ text key ]
        , span [ class "profile-value" ] [ text (toString val) ]
        ]
    True ->
      div [ class "profile-element" ]
        [ span [ class "profile-label" ] [ text key ]
        , span [ class "profile-input-group" ]
            [ input
                [ type' "number"
                , value (toString val)
                , step "0.001"
                , on "input" targetValue (\str ->
                    case (String.toFloat str) of
                      Ok v -> Signal.message address (SetAttribute
                        activityType (SetAttr (SetSpecific key v)))
                      Err _ -> Signal.message address NoOp)
                ] []
            ]
        ]
