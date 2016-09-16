module Main.Actions where


-- Elm Packages
import History exposing (setPath)
import Effects exposing (Effects)
import Task
-- Trifecta Modules
import Main.Model exposing (Model)
import SignIn.Actions
import UserProfile.Actions
import UserProfile.Model exposing (User)
import Dashboard.Actions
import Dashboard.Model exposing (TrainingPlan)


type Action
  = NoOp
  | Init
  | AppLink String
  | SignIn SignIn.Actions.Action
  | SetUser User
  | UserProfile UserProfile.Actions.Action
  | InitTrainingPlans (List TrainingPlan)
  | LoadDashboard User
  | LoadOnboarding User
  | EmailSignIn
  | EmailSignUp
  | Dashboard Dashboard.Actions.Action
  | MailingListSignUp
