module Main.View where


-- Elm Packages
import Signal exposing (Address)
import Html exposing (Html, div, span, text, img, nav, button)
import Html.Attributes exposing (class, src, alt, value, type')
import Html.Events exposing (onClick)
-- Trifecta Modules
import Main.Actions exposing (..)
import Main.Model exposing (..)
import Dashboard.Actions as Dash


lostView : Address Action -> String -> String -> Int -> Model -> Html
lostView address _ _ _ _ =
  div [ class "lost-container" ]
    [ div
        [ class "col-xs-10 col-xs-offset-1 col-md-8 col-md-offset-0" ]
        [ div [ class "lost-header" ] [ text "404" ]
        , div 
            [ class "lost-subheader" ]
            [ text "Lost? Looks like you took a wrong turn" ]
        , button 
            [ class "btn tf-btn-primary lost-content"
            , type' "button"
            , onClick address (AppLink "/")
            ]
            [ text "Take me home" ]
        ]
    ]


userAvatar : Address Dash.Action -> Model -> Html
userAvatar address model =
  case model.userProfile.currentUser of
    Nothing -> span [] []
    Just user ->
      let
        avatarURL = case user.avatarURL of
          Nothing -> "assets/img/anon.png"
          Just url -> url
      in
        img
          [ class "user-profile"
          , src (avatarURL)
          , onClick address Dash.Profile
          ] []


appView : Address Dash.Action -> Model -> List Html -> Html
appView address model children =
  div []
    ((nav [ class "navbar navbar-fixed-top dashnav-container" ]
        [ div [ class "container-fluid dashnav" ]
            [ img
                [ class "header-logo"
                , alt "Trifecta Logo"
                , src "assets/img/trifecta.svg"
                , onClick address Dash.TrainingPlans
                ] []
            , div [ class "header-right" ]
                [ userAvatar address model
                , span
                    [ class "link-icon glyphicon glyphicon-log-out"
                    , onClick address Dash.SignOut
                    ] []
                ]
            ]
        ]) :: children)
