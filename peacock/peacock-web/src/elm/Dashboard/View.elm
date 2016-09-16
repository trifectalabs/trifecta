module Dashboard.View where


-- Elm Packages
import Signal exposing (Address)
import Html exposing (Html, div, span, text, img, nav, a, button, input)
import Html.Attributes exposing (class, classList, src, alt, href)
import Html.Events exposing (onClick)
import Maybe exposing (withDefault)
import Date exposing (Date, fromTime)
import Date.Format as DateFormatter
import Number.Format as NumberFormatter
import Dict
import String exposing (join)
-- Trifecta Modules
import Main.Actions as Main
import Main.Model as M
import Main.View exposing (appView)
import Util.Types exposing (..)
import Dashboard.Actions exposing (..)
import Dashboard.Model exposing (..)


plansView : Address Action -> String -> String -> Int -> M.Model -> Html
plansView address _ hash length model =
  appView address model
    [ div [ class "dashboard" ]
        ((span [ class "dashboard-header" ] [ text "Training Plans" ]) ::
          (List.map trainingPlanView model.dashboard.trainingPlans))
    ]


trainingPlanView : TrainingPlan -> Html
trainingPlanView trainingPlan =
  div [ class "training-plan" ]
    [ activityTypeIcon trainingPlan.activityType
    , div [ class "training-plan-content" ]
        ((dateSpan trainingPlan.startDate trainingPlan.endDate) ::
        (List.map trainingPlanActivityView trainingPlan.activities))
    ]


activityTypeIcon : ActivityType -> Html
activityTypeIcon activityType =
  span
    [ classList
      [ ("activity-type", True)
      , ("icon-ride", activityType == Ride)
      , ("icon-run", activityType == Run)
      , ("icon-swim", activityType == Swim)
      ]
    ] []


dateSpan: Date -> Date -> Html
dateSpan start end =
  div [ class "date-span" ]
    [ span [ class "date" ] [ text (DateFormatter.format "%b %e, %Y" start) ]
    , span [ class "date-divider" ] [ text "-" ]
    , span [ class "date" ] [ text (DateFormatter.format "%b %e, %Y" end) ]
    ]


trainingPlanActivityView : TrainingPlanActivity -> Html
trainingPlanActivityView activity =
  div [ class "activity" ]
    [ span [ class "distance" ]
        [ span [ class "value-label" ] [ text "Disance:" ]
        , span [ class "number" ] [ text (displayFloat activity.distance) ]
        ]
    , span [ class "time" ]
        [ span [ class "value-label" ] [ text "Time:" ]
        , span [ class "number" ] [ text (displayFloat activity.time) ]
        ]
    , span [ class "elevation" ]
        [ span [ class "value-label" ] [ text "Elevation:" ]
        , span [ class "number" ] [ text (displayFloat activity.elevation) ]
        ]
    ]


displayFloat : Float -> String
displayFloat float =
  NumberFormatter.pretty 1 ',' float
