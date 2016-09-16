module UserProfile.Actions where


-- Elm Packages
import Date exposing (Date)
-- Trifecta Modules
import UserProfile.Model exposing (User)
import Util.Types exposing (..)


type AttributeAction
  = AddAttribute
  | RemoveAttribute
  | SetDays Int
  | SetActivities Int
  | SetLongest Float
  | SetShortFraction Float
  | SetAverageFraction Float
  | SetLongFraction Float
  | SetSpecific String Float


type EditAction
  = EditValue
  | CancelEdit
  | SaveValue
  | SetString String
  | SetFloat Float
  | SetDate Date
  | SetAttr AttributeAction


type Action
  = NoOp
  | Onboard
  | SaveProfile
  | SetFirstName EditAction
  | SetLastName EditAction
  | SetEmail EditAction
  | SetCity EditAction
  | SetProvince EditAction
  | SetCountry EditAction
  | SetSex EditAction
  | SetBirthday EditAction
  | SetAvatar EditAction
  | SetHeight EditAction
  | SetWeight EditAction
  | SetAttribute ActivityType EditAction
