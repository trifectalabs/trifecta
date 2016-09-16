module Main.Model where


-- Trifecta Modules
import SignIn.Model as SignIn
import UserProfile.Model as UserProfile
import Dashboard.Model as Dashboard


type alias Model =
  { signInForm : SignIn.Model
  , userProfile : UserProfile.Model
  , dashboard : Dashboard.Model
  }


initModel =
  { signInForm =
      SignIn.Model Nothing Nothing "" ""
  , userProfile =
      UserProfile.Model Nothing Nothing UserProfile.defaultEditing
  , dashboard =
      Dashboard.Model []
  }
