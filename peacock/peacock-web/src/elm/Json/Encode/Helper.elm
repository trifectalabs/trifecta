module Json.Encode.Helper where


-- Elm Packages
import Json.Encode exposing (..)
import Date exposing (Date)
import Date.Format as DateFormatter


date : Date -> Value
date d =
  string (DateFormatter.format "%Y-%m-%d" d)


time : Date -> Value
time d =
  string (DateFormatter.format "%H:%M:%S" d)


dateTime : Date -> Value
dateTime d =
  string (DateFormatter.format "%Y-%m-%dT%H:%M:%SZ" d)
