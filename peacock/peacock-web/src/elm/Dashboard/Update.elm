module Dashboard.Update where


-- Elm Packages
import History exposing (setPath)
import Task exposing (Task, onError, andThen)
import Effects exposing (Effects)
import Http exposing (get, send, defaultSettings, Response, empty)
import Date exposing (Date, fromTime)
import Json.Decode as Json
-- Trifecta Modules
import Json.Decode.Internal exposing (trainingPlanDecoder)
import Dashboard.Model exposing (..)
import Dashboard.Actions exposing (..)


update : Action -> Model -> (Model, Effects Action)
update action model =
  case action of
    FetchTrainingPlans userID offset limit ->
      let
        off = toString offset
        lim = toString limit
        url = "api/trainingplans/" ++ userID ++ "/" ++ off ++ "/" ++ lim
        request = get (Json.list trainingPlanDecoder) url
        response = (Task.map (\p -> SetTrainingPlans p) request)
          `onError` (\err -> Task.succeed NoOp)
      in
        (model, Effects.task response)
    SetTrainingPlans plans ->
      ({ model | trainingPlans = plans }, Effects.none)
    Profile ->
      let
        profile = setPath "/profile"
          |> Task.map (\p -> NoOp)
      in
        (model, Effects.task profile)
    TrainingPlans ->
        let
          dash = setPath "/dashboard"
            |> Task.map (\p -> NoOp)
        in
          (model, Effects.task dash)
    SignOut ->
      let
        request =
          { verb = "GET"
          , headers = []
          , url = "signout"
          , body = empty
          }
        response =
          Task.map handleSignOut (send defaultSettings request)
            `onError` (\err -> Task.succeed NoOp)
      in
        (model, Effects.task response)
    Leave ->
      let
        leave = setPath "/signin"
          |> Task.map (\p -> NoOp)
      in
        (model, Effects.task leave)
    NoOp ->
      (model, Effects.none)


handleSignOut : Response -> Action
handleSignOut response =
  case response.status of
    200 -> Leave
    _ -> NoOp
