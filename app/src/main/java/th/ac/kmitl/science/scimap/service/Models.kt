package th.ac.kmitl.science.scimap.service

data class Area(var ID: Int, var DisplayName: String, var AreaPlanImage: String )

data class Building(var ID: Int, var DisplayName: String, var PolygonArea: Array<Coordinate>, var AreaID: Int)

data class Coordinate(var x: Int, var y: Int)