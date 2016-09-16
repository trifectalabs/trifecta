-- Elm Packages
import Router exposing (Route, match, (:->))
import History exposing (path, hash, length)
import Task exposing (Task)
import Signal exposing (Address, Mailbox, mailbox)
import Effects exposing (Effects)
import Html exposing (Html)
-- Trifecta Modules
import Main.Actions exposing (..)
import Main.Update exposing (update)
import Main.Model exposing (Model, initModel)
import Main.View exposing (lostView)
import SignIn.View exposing (stravaLandingView, mailingLandingView)
import UserProfile.View exposing (profileView)
import Dashboard.View exposing (plansView)


route : Route (String -> Int -> Model -> Html)
route = match
  [ "/" :-> (mailingLandingView address notifications.address)
  , "/signup" :-> (stravaLandingView address notifications.address)
  , "/signin" :-> (stravaLandingView address notifications.address)
  , "/dashboard" :-> (plansView (Signal.forwardTo address Dashboard))
  , "/onboarding" :-> (profileView
    (Signal.forwardTo address Dashboard) (Signal.forwardTo address UserProfile))
  , "/profile" :-> (profileView
    (Signal.forwardTo address Dashboard) (Signal.forwardTo address UserProfile))
  ] (lostView address)


messages : Mailbox (List Action)
messages =
  mailbox []


singleton : a -> List a
singleton action =
  [ action ]


address : Address Action
address =
  Signal.forwardTo messages.address singleton


effectsAndModel : Signal (Model, Effects Action)
effectsAndModel =
  Signal.foldp
    (update notifications.address)
    (initModel, Effects.task (Task.succeed Init))
    messages.signal


model : Signal Model
model =
  Signal.map fst effectsAndModel


port tasks : Signal (Task Effects.Never ())
port tasks =
  Signal.map (Effects.toTask messages.address << snd) effectsAndModel


notifications : Mailbox (String, String)
notifications =
  mailbox ("", "")


port notifs : Signal (String, String)
port notifs =
  notifications.signal


main =
  Signal.map4
    route
    path
    hash
    length
    model
