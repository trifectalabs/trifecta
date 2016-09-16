module SignIn.Update where


-- Elm Packages
import Signal exposing (Address)
import Effects exposing (Effects)
import Http
import List exposing (append)
-- Trifecta Modules
import SignIn.Actions exposing (..)
import SignIn.Model exposing (..)


update : Action -> Model -> (Model, Effects Action)
update action model =
  case action of
    SetFirstName newName ->
      ({ model | firstName = Just newName }, Effects.none)
    SetLastName newName ->
      ({ model | lastName = Just newName }, Effects.none)
    SetEmail newEmail ->
      ({ model | email = newEmail }, Effects.none)
    SetPassword newPassword ->
      ({ model | password = newPassword }, Effects.none)


processForm : Model -> Http.Body
processForm form =
  let
    firstName = case form.firstName of
      Nothing -> []
      Just name -> [ Http.stringData "firstName" name ]
    lastName = case form.lastName of
      Nothing -> []
      Just name -> [ Http.stringData "lastName" name ]
  in
    Http.multipart
      (firstName `append` lastName `append`
      [ Http.stringData "email" form.email
      , Http.stringData "password" form.password
      ])
