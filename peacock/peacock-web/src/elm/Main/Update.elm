module Main.Update where


-- Elm Packages
import Signal exposing (Address)
import History exposing (setPath)
import Effects exposing (Effects)
import Task exposing (Task, onError, andThen)
import Http exposing (
  send, defaultSettings, empty, fromJson, Request, Response, get, post)
import Json.Decode as Json
-- Trifecta Modules
import Main.Model exposing (Model)
import Main.Actions exposing (..)
import SignIn.Update as SignIn exposing (processForm)
import UserProfile.Update as UserProfile
import UserProfile.Model exposing (User)
import Dashboard.Update as Dashboard
import Json.Decode.Internal exposing (userDecoder, trainingPlanDecoder)


updateStep : Address (String, String) ->
  Action -> (Model, Effects Action) -> (Model, Effects Action)
updateStep notifs action (oldModel, accumulatedEffects) =
  let
    (newModel, additionalEffects) = update' notifs action oldModel
  in
    (newModel, Effects.batch [accumulatedEffects, additionalEffects])


update : Address (String, String) ->
  List Action -> (Model, Effects Action) -> (Model, Effects Action)
update notifs actions (model, _) =
  List.foldl (updateStep notifs) (model, Effects.none) actions


update' : Address (String, String) -> Action -> Model -> (Model, Effects Action)
update' notifs action model =
  case action of
    Init ->
      let
        request =
          { verb = "GET"
          , headers = []
          , url = "api/init"
          , body = empty
          }
        response =
          (fromJson userDecoder (send defaultSettings request)
            `andThen` (\u -> Task.succeed (SetUser u)))
            `onError` (\_ -> Task.succeed NoOp)
      in
        (model, Effects.task response)
    AppLink path' ->
      let
        switch = setPath path'
          |> Task.map (\p -> NoOp)
      in
        (model, Effects.task switch)
    SignIn action ->
      let
        (newForm, signInEffects) = SignIn.update action model.signInForm
        effects = Effects.map (\eff -> SignIn eff) signInEffects
      in
        ({ model | signInForm = newForm }, effects)
    LoadDashboard user ->
      let
        task = (setPath "/dashboard"
          `onError` (\_ -> Task.succeed ()))
          `andThen` (\_ -> Task.succeed (SetUser user))
      in
        (model, Effects.task task)
    LoadOnboarding user ->
      let
        task = (setPath "/onboarding"
          `onError` (\_ -> Task.succeed ()))
          `andThen` (\_ -> Task.succeed (SetUser user))
      in
        (model, Effects.task task)
    SetUser newUser ->
      let
        url = "api/trainingplans/" ++ newUser.id ++ "/0/20"
        request = get (Json.list trainingPlanDecoder) url
        response = (Task.map (\p -> InitTrainingPlans p) request)
          `onError` (\err -> Task.succeed NoOp)
        currentProfile = model.userProfile
        newProfile =
          { currentProfile |
              currentUser = Just newUser,
              profileForm = Just newUser
          }
      in
        ({ model | userProfile = newProfile }, Effects.task response)
    UserProfile action ->
      let
        (newModel, profileEffects) = UserProfile.update action model.userProfile
        effects = Effects.map (\eff -> UserProfile eff) profileEffects
      in
        ({ model | userProfile = newModel }, effects)
    InitTrainingPlans plans ->
      let
        currentDash = model.dashboard
        newDash = { currentDash | trainingPlans = plans }
      in
        ({ model | dashboard = newDash }, Effects.none)
    EmailSignUp ->
      let
        next = LoadOnboarding
        form = processForm model.signInForm
        request =
          { verb = "POST"
          , headers = []
          , url = "signup"
          , body = form
          }
        response = handleSignIn notifs next request
      in
        (model, Effects.task response)
    EmailSignIn ->
      let
        next = LoadDashboard
        form = processForm model.signInForm
        request =
          { verb = "POST"
          , headers = []
          , url = "authenticate/credentials"
          , body = form
          }
        response = handleSignIn notifs next request
      in
        (model, Effects.task response)
    Dashboard action ->
      let
        (newModel, dashboardEffects) = Dashboard.update action model.dashboard
        effects = Effects.map (\eff -> Dashboard eff) dashboardEffects
      in
        ({ model | dashboard = newModel }, effects)
    MailingListSignUp ->
      let
        url = "api/mailinglist/add/" ++ model.signInForm.email
        request =
          { verb = "POST"
          , headers = []
          , url = url
          , body = empty
          }
        result = 
          (Task.map handleMailingList (send defaultSettings request))
            `andThen` (\n -> 
              Signal.send notifs n
                `andThen` (\_ -> Task.succeed NoOp))
            `onError` (\_ ->
              Signal.send 
                notifs ("Whoops, looks like something went wrong :(", "danger")
                `andThen` (\_ -> Task.succeed NoOp))                
      in
        (model, Effects.task result)
    NoOp ->
      (model, Effects.none)


handleSignIn : Address (String, String) -> (User -> Action) ->
  Request -> Task Effects.Never Action
handleSignIn notifs next request =
  (fromJson userDecoder (send defaultSettings request)
    `andThen` (\u -> Task.succeed (next u)))
    `onError` (\err -> case err of
      Http.BadResponse 409 _ ->
        Signal.send notifs
          ("The email you entered is already registered", "danger")
          `andThen` (\_ -> Task.succeed NoOp)
      Http.BadResponse 401 _ ->
        Signal.send notifs
          ("The email or password you entered is incorrect", "danger")
          `andThen` (\_ -> Task.succeed NoOp)
      _ ->
        Task.succeed NoOp)

handleMailingList : Response -> (String, String)
handleMailingList response =
  case response.status of
    204 -> ("We'll keep in touch :)", "success")
    _ -> ("Whoops, looks like something went wrong :(", "danger")

