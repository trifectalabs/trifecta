module Dashboard.Actions where


-- Trifecta Modules
import Dashboard.Model exposing (TrainingPlan)


type Action
  = NoOp
  | FetchTrainingPlans String Int Int
  | SetTrainingPlans (List TrainingPlan)
  | Profile
  | TrainingPlans
  | SignOut
  | Leave
