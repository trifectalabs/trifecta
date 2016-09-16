package models

case class ActivitySearchSpace(
  short: SearchSpace,
  avg: SearchSpace,
  long: SearchSpace)

case class SearchSpace(
  time: ParameterSearchSpace,
  distance: ParameterSearchSpace,
  elevation: ParameterSearchSpace)

case class ParameterSearchSpace(
  lower: Double,
  upper: Double)
