module Json.Decode.Helper where


-- Elm Packages
import Json.Decode exposing (..)
import Date exposing (Date, fromTime)


apply : Decoder (a -> b) -> Decoder a -> Decoder b
apply func value =
    object2 (<|) func value


dateDecoder : Decoder Date
dateDecoder = customDecoder float (\t -> Result.Ok (fromTime t))
