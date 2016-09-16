module SignIn.View where


-- Elm Packages
import Signal exposing (Address)
import Html exposing (
  Html, div, span, text, form, input, button, a, label, img, nav)
import Html.Attributes exposing (class, classList, name, type', placeholder,
  value, action, method, checked, src, alt, href)
import Html.Events exposing (
  onClick, on, targetValue, onKeyPress)
import Maybe exposing (withDefault)
-- Trifecta Modules
import Main.Actions exposing (..)
import Main.Model exposing (..)
import SignIn.Actions as Sign
import SignIn.Model as Sn


-- Main Page with only strava authenication
stravaLandingView : Address Action -> Address (String, String) ->
  String -> String -> Int -> Model -> Html
stravaLandingView address notifs _ hash length model =
  let
    landingHeader =
      "Discover a new way to train."
    landingSubline =
      "Convenient and precise training plans for every skill level"
  in
    div [ class "container-fluid landing-container" ]
      [ nav [ class "navbar navbar-fixed-top" ]
          [ div [ class "container-fluid" ]
              [ a [ class "navbar-brand", href "#" ]
                  [ img
                      [ class "header-logo"
                      , alt "Trifecta Logo"
                      , src "assets/img/trifecta-white.svg"
                      ] []
                  ]
              , div [ class "sign-switch-container" ]
                  [ a
                      [ class "sign-switch"
                      , href "authenticate/strava"
                      ] [ text "Sign In" ]
                  ]
              ]
          ]
      , div [ class "landing-main" ]
          [ div [ class "landing-header" ]
              [ text landingHeader ]
          , div [ class "landing-sub" ]
              [ text landingSubline ]
          , stravaButton "Sign up with Strava"
          ]
      ]


-- Main Page with mailing list sign up
mailingLandingView : Address Action -> Address (String, String) ->
  String -> String -> Int -> Model -> Html
mailingLandingView address notifs _ hash length model =
  let
    landingHeader =
      "Discover a new way to train."
  in
    div [ class "landing-container" ]
      [ div 
          [ class ("col-xs-10 col-xs-offset-1 " ++ 
            "col-md-6 col-md-offset-3 " ++
            "col-lg-4 col-lg-offset-4 landing-main")
          ]
          [ img
              [ class "main-logo"
              , alt "Trifecta Logo"
              , src "assets/img/trifecta.svg"
              ] []
          , div [ class "landing-header" ]
              [ text landingHeader ]
          , mailingListSignUp address model
          ]
      ]


mailingListSignUp : Address Action -> Model -> Html
mailingListSignUp address model =
  div []
    [ input 
        [ type' "text"
        , class "mailing-list-email" 
        , name "mailing-list-email" 
        , placeholder "Your email"
        , value model.signInForm.email
        , on "input" targetValue
          (\str -> Signal.message address (SignIn (Sign.SetEmail str)))
        , onKeyPress address (enterKeyPress MailingListSignUp)
        ] []
    , button 
        [ class "btn tf-btn-primary"
        , type' "button" 
        , onClick address MailingListSignUp
        ] 
        [ text "Keep me in the loop" ]
    ]


type PageType = LandingPage | SignUpPage | SignInPage


landingView : Address Action -> Address (String, String) ->
  String -> String -> Int -> Model -> Html
landingView address notifs _ hash length model =
  view LandingPage address notifs model.signInForm


signUpView : Address Action -> Address (String, String) ->
  String -> String -> Int -> Model -> Html
signUpView address notifs _ hash length model =
  view SignUpPage address notifs model.signInForm


signInView : Address Action -> Address (String, String) ->
  String -> String -> Int -> Model -> Html
signInView address notifs _ hash length model =
  view SignInPage address notifs model.signInForm


view : PageType -> Address Action -> Address (String, String) ->
  Sn.Model -> Html
view pageType address notifs model =
  let
    (signText, appLink) = case pageType of
      SignInPage -> ("Sign Up", AppLink "/signup")
      _ -> ("Sign In", AppLink "/signin")
  in
    div [ class "container-fluid landing-container" ]
      ((nav [ class "navbar navbar-fixed-top" ]
          [ div [ class "container-fluid" ]
              [ a [ class "navbar-brand", href "#" ]
                  [ img
                      [ class "header-logo"
                      , alt "Trifecta Logo"
                      , src "assets/img/trifecta-white.svg"
                      ] []
                  ]
              , div [ class "sign-switch-container" ]
                  [ a
                      [ class "sign-switch"
                      , onClick address appLink
                      ] [ text signText ]
                  ]
              ]
          ]
      ) :: (mainView pageType address notifs model))


mainView : PageType -> Address Action -> Address (String, String) ->
  Sn.Model -> List Html
mainView pageType address notifs model =
  case pageType of
    LandingPage ->
      let
        landingHeader =
          "Discover a new way to train."
        landingSubline =
          "Convenient and precise training plans for every skill level"
      in
        [ div [ class "landing-main" ]
            [ div [ class "landing-header" ]
                [ text landingHeader ]
            , div [ class "landing-sub" ]
                [ text landingSubline ]
            , a
                [ class "btn tf-btn-primary"
                , type' "button"
                , onClick address (AppLink "/signup")
                ] [ text "Try It Now" ]
            ]
        ]
    _ ->
      let
        -- This is silly but I'm lazy
        (signText1, signText2, signText3, signPath,
        signForm, signAction, signLabel, tagLine) =
          case pageType of
            SignUpPage -> ("Sign up", "sign up", "Sign in ", "/signin",
              signUpForm, EmailSignUp, "Sign Up",
              "Discover a new way to train.")
            _ -> ("Sign in", "sign in", "Sign up ", "/signup",
              signInForm, EmailSignIn, "Sign In",
              "Your training companion.")
      in
        [ div [ class "tag-line" ] [ text tagLine ]
        , div [ class "sign-up-main" ]
            [ stravaButton (signText1 ++ " with Strava")
            , facebookButton (signText1 ++ " with Facebook")
            , googleButton (signText1 ++ " with Google")
            , span
                [ class "email-divider" ]
                [ text ("or " ++ signText2 ++ " with email") ]
            , signForm address model
            , button
                [ class "btn tf-btn-primary"
                , type' "button"
                , onClick address signAction
                ] [ text signLabel ]
            , span [ class "footer" ]
                [ text ("Already a member? " ++ signText3)
                , a [ onClick address (AppLink signPath) ] [ text "here." ]
                ]
            ]
        ]


stravaButton : String -> Html
stravaButton buttonText =
  a [ class "btn btn-block btn-social btn-soundcloud"
    , href "authenticate/strava"
    ]
    [ span [ class "icon-strava" ] []
    , text buttonText
    ]


facebookButton : String -> Html
facebookButton buttonText =
  a [ class "btn btn-block btn-social btn-facebook"
    , href "authenticate/facebook"
    ]
    [ span [ class "fa fa-facebook" ] []
    , text buttonText
    ]


googleButton : String -> Html
googleButton buttonText =
  a [ class "btn btn-block btn-social btn-google"
    , href "authenticate/google"
    ]
    [ span [ class "fa fa-google" ] []
    , text buttonText
    ]


signUpForm : Address Action -> Sn.Model -> Html
signUpForm address model =
  form []
    [ input
        [ class "first-name"
        , name "firstName"
        , type' "text"
        , placeholder "First Name"
        , value (withDefault "" model.firstName)
        , on "input" targetValue
          (\str -> Signal.message address (SignIn (Sign.SetFirstName str)))
        , onKeyPress address (enterKeyPress EmailSignUp)
        ] []
    , input
        [ class "last-name"
        , name "lastName"
        , type' "text"
        , placeholder "Last Name"
        , value (withDefault "" model.lastName)
        , on "input" targetValue
          (\str -> Signal.message address (SignIn (Sign.SetLastName str)))
        , onKeyPress address (enterKeyPress EmailSignUp)
        ] []
    , input
        [ class "email"
        , name "email"
        , type' "text"
        , placeholder "Email"
        , value model.email
        , on "input" targetValue
          (\str -> Signal.message address (SignIn (Sign.SetEmail str)))
        , onKeyPress address (enterKeyPress EmailSignUp)
        ] []
    , input
        [ class "password"
        , name "password"
        , type' "password"
        , placeholder "Password"
        , value model.password
        , on "input" targetValue
          (\str -> Signal.message address (SignIn (Sign.SetPassword str)))
        , onKeyPress address (enterKeyPress EmailSignUp)
        ] []
    ]


signInForm : Address Action -> Sn.Model -> Html
signInForm address model =
  form []
    [ input
        [ class "email"
        , name "email"
        , type' "text"
        , placeholder "Email"
        , value model.email
        , on "input" targetValue
          (\str -> Signal.message address (SignIn (Sign.SetEmail str)))
        , onKeyPress address (enterKeyPress EmailSignIn)
        ] []
    , input
        [ class "password"
        , name "password"
        , type' "password"
        , placeholder "Password"
        , value model.password
        , on "input" targetValue
          (\str -> Signal.message address (SignIn (Sign.SetPassword str)))
        , onKeyPress address (enterKeyPress EmailSignIn)
        ] []
    ]


enterKeyPress : Action -> Int -> Action
enterKeyPress action key =
  case key of
    13 -> action
    _ -> NoOp
