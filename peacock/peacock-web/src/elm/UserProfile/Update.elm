module UserProfile.Update where


-- Elm Packages
import Effects exposing (Effects)
import Maybe exposing (withDefault)
import Dict
import Task exposing (onError, andThen)
import Http exposing (defaultSettings, send)
import Json.Encode as Json
import History exposing (setPath)
-- Trifecta Modules
import Json.Encode.Internal exposing (userEncoder)
import Util.Types exposing (..)
import UserProfile.Actions exposing (..)
import UserProfile.Model exposing (..)


update : Action -> Model -> (Model, Effects Action)
update action model =
  let
    currentForm = withDefault defaultUser model.profileForm
    newForm = case action of
      SetFirstName (SetString newName) ->
        { currentForm | firstName = Just newName }
      SetLastName (SetString newName) ->
        { currentForm | lastName = Just newName }
      SetEmail (SetString newEmail) ->
        { currentForm | email = Just newEmail }
      SetCity (SetString newCity) ->
        { currentForm | city = Just newCity }
      SetProvince (SetString newProvince) ->
        { currentForm | province = Just newProvince }
      SetCountry (SetString newCountry) ->
        { currentForm | country = Just newCountry }
      SetSex (SetString newSex) ->
        { currentForm | sex = Just newSex }
      SetBirthday (SetDate newBirthday) ->
        { currentForm | dateOfBirth = Just newBirthday }
      SetHeight (SetFloat newHeight) ->
        { currentForm | height = Just newHeight }
      SetWeight (SetFloat newWeight) ->
        { currentForm | weight = Just newWeight }
      SetAttribute activityType (SetAttr attributeAction) ->
        attributeUpdate activityType attributeAction currentForm
      _ ->
        currentForm
    currentModel = withDefault defaultUser model.currentUser
    (newModel, effects) = case action of
      SetFirstName SaveValue ->
        ({ currentModel |
            firstName = newForm.firstName,
            lastName = newForm.lastName },
          Effects.task (Task.succeed SaveProfile))
      SetLastName SaveValue ->
        ({ currentModel |
            firstName = newForm.firstName,
            lastName = newForm.lastName },
          Effects.task (Task.succeed SaveProfile))
      SetEmail SaveValue ->
        ({ currentModel | email = newForm.email },
          Effects.task (Task.succeed SaveProfile))
      SetCity SaveValue ->
        ({ currentModel |
            city = newForm.city,
            province = newForm.province,
            country = newForm.country },
          Effects.task (Task.succeed SaveProfile))
      SetProvince SaveValue ->
        ({ currentModel |
            city = newForm.city,
            province = newForm.province,
            country = newForm.country },
          Effects.task (Task.succeed SaveProfile))
      SetCountry SaveValue ->
        ({ currentModel |
            city = newForm.city,
            province = newForm.province,
            country = newForm.country },
          Effects.task (Task.succeed SaveProfile))
      SetSex SaveValue ->
        ({ currentModel | sex = newForm.sex },
          Effects.task (Task.succeed SaveProfile))
      SetBirthday SaveValue ->
        ({ currentModel | dateOfBirth = newForm.dateOfBirth },
          Effects.task (Task.succeed SaveProfile))
      SetHeight SaveValue ->
        ({ currentModel | height = newForm.height },
          Effects.task (Task.succeed SaveProfile))
      SetWeight SaveValue ->
        ({ currentModel | weight = newForm.weight },
          Effects.task (Task.succeed SaveProfile))
      SetAttribute activityType SaveValue ->
        (attributeSave activityType currentModel newForm,
          Effects.task (Task.succeed SaveProfile))
      SetAttribute activityType (SetAttr AddAttribute) ->
        (attributeSave activityType currentModel newForm,
          Effects.task (Task.succeed SaveProfile))
      SetAttribute activityType (SetAttr RemoveAttribute) ->
        (attributeSave activityType currentModel newForm,
          Effects.task (Task.succeed SaveProfile))
      _ ->
        (currentModel, Effects.none)
    currentEditing = model.editing
    newEditing = case action of
      SetFirstName editAction -> case editAction of
        EditValue -> { currentEditing | name = True }
        CancelEdit -> { currentEditing | name = False }
        SaveValue -> { currentEditing | name = False }
        _ -> currentEditing
      SetLastName editAction -> case editAction of
        EditValue -> { currentEditing | name = True }
        CancelEdit -> { currentEditing | name = False }
        SaveValue -> { currentEditing | name = False }
        _ -> currentEditing
      SetEmail editAction -> case editAction of
        EditValue -> { currentEditing | email = True }
        CancelEdit -> { currentEditing | email = False }
        SaveValue -> { currentEditing | email = False }
        _ -> currentEditing
      SetCity editAction -> case editAction of
        EditValue -> { currentEditing | location = True }
        CancelEdit -> { currentEditing | location = False }
        SaveValue -> { currentEditing | location = False }
        _ -> currentEditing
      SetProvince editAction -> case editAction of
        EditValue -> { currentEditing | location = True }
        CancelEdit -> { currentEditing | location = False }
        SaveValue -> { currentEditing | location = False }
        _ -> currentEditing
      SetCountry editAction -> case editAction of
        EditValue -> { currentEditing | location = True }
        CancelEdit -> { currentEditing | location = False }
        SaveValue -> { currentEditing | location = False }
        _ -> currentEditing
      SetSex editAction -> case editAction of
        EditValue -> { currentEditing | sex = True }
        CancelEdit -> { currentEditing | sex = False }
        SaveValue -> { currentEditing | sex = False }
        _ -> currentEditing
      SetBirthday editAction -> case editAction of
        EditValue -> { currentEditing | birthday = True }
        CancelEdit -> { currentEditing | birthday = False }
        SaveValue -> { currentEditing | birthday = False }
        _ -> currentEditing
      SetHeight editAction -> case editAction of
        EditValue -> { currentEditing | height = True }
        CancelEdit -> { currentEditing | height = False }
        SaveValue -> { currentEditing | height = False }
        _ -> currentEditing
      SetWeight editAction -> case editAction of
        EditValue -> { currentEditing | weight = True }
        CancelEdit -> { currentEditing | weight = False }
        SaveValue -> { currentEditing | weight = False }
        _ -> currentEditing
      SetAttribute activityType editAction ->
        let
          key = toString activityType
          currentAttr = currentEditing.attributes
        in
          case editAction of
            EditValue ->
              let newAttr = Dict.update key (\v -> Just True) currentAttr
              in { currentEditing | attributes = newAttr }
            CancelEdit ->
              let newAttr = Dict.update key (\v -> Just False) currentAttr
              in { currentEditing | attributes = newAttr }
            SaveValue ->
              let newAttr = Dict.update key (\v -> Just False) currentAttr
              in { currentEditing | attributes = newAttr }
            _ -> currentEditing
      _ ->
        currentEditing
  in
    case action of
      SaveProfile ->
        let
          request =
            { verb = "POST"
            , headers = [("Content-Type", "application/json")]
            , url = "api/user"
            , body = Http.string (Json.encode 0 (withDefault Json.null
                (Maybe.map userEncoder model.currentUser)))
            }
          response = (Task.map (\_ -> NoOp) (send defaultSettings request))
            `onError` (\_ -> Task.succeed NoOp)
        in
          (model, Effects.task response)
      Onboard ->
        let
          task = (setPath "/dashboard"
            `onError` (\_ -> Task.succeed ()))
            `andThen` (\_ -> Task.succeed SaveProfile)
        in
          (model, Effects.task task)
      _ ->
        ({ model |
            currentUser = Just newModel,
            profileForm = Just newForm,
            editing = newEditing }, effects)


attributeSave : ActivityType -> User -> User -> User
attributeSave activityType model form =
  let
    type' = toString activityType
    newAttributes = case Dict.get type' form.attributes of
      Just currentAttributes ->
        Dict.update type' (\attr -> Just currentAttributes) model.attributes
      Nothing ->
        Dict.remove type' model.attributes
  in
    { model | attributes = newAttributes }


attributeUpdate : ActivityType -> AttributeAction -> User -> User
attributeUpdate activityType action form =
  let
    type' = toString activityType
    newAttributes = case Dict.get type' form.attributes of
      Just currentAttributes ->
        case action of
          SetDays newDays ->
            let updateAttr = { currentAttributes | days = newDays } in
            Dict.update type' (\attr -> Just updateAttr) form.attributes
          SetActivities newActs ->
            let updateAttr = { currentAttributes | activities = newActs } in
            Dict.update type' (\attr -> Just updateAttr) form.attributes
          SetLongest newDist ->
            let updateAttr = { currentAttributes | longestDistance = newDist }
            in
              Dict.update type' (\attr -> Just updateAttr) form.attributes
          SetShortFraction newFrac ->
            let
              currentVariance = currentAttributes.variance
              newVariance = { currentVariance | fractionShort = newFrac }
              updateAttr = { currentAttributes | variance = newVariance }
            in
              Dict.update type' (\attr -> Just updateAttr) form.attributes
          SetAverageFraction newFrac ->
            let
              currentVariance = currentAttributes.variance
              newVariance = { currentVariance | fractionAverage = newFrac }
              updateAttr = { currentAttributes | variance = newVariance }
            in
              Dict.update type' (\attr -> Just updateAttr) form.attributes
          SetLongFraction newFrac ->
            let
              currentVariance = currentAttributes.variance
              newVariance = { currentVariance | fractionLong = newFrac }
              updateAttr = { currentAttributes | variance = newVariance }
            in
              Dict.update type' (\attr -> Just updateAttr) form.attributes
          SetSpecific key newVal ->
            let
              currentSpec = currentAttributes.activitySpecific
              newSpec = Dict.update key (\v -> Just newVal) currentSpec
              updateAttr = { currentAttributes | activitySpecific = newSpec }
            in
              Dict.update type' (\attr -> Just updateAttr) form.attributes
          _ ->
            form.attributes
      Nothing -> form.attributes
  in
    case action of
      AddAttribute ->
        case (Dict.get type' defaultAttributes) of
          Just attr ->
            { form | attributes = Dict.insert type' attr form.attributes }
          Nothing ->
            form
      RemoveAttribute ->
        { form | attributes = Dict.remove type' form.attributes }
      _ ->
        { form | attributes = newAttributes }
