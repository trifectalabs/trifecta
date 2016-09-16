-- Function: diamond_route(double precision, double precision, double precision, double precision, double precision, double precision, double precision, double precision)

-- DROP FUNCTION diamond_route(double precision, double precision, double precision, double precision, double precision, double precision, double precision, double precision);

CREATE OR REPLACE FUNCTION trifecta_diamond_route(
    IN y1 double precision,
    IN x1 double precision,
    IN y2 double precision,
    IN x2 double precision,
    IN y3 double precision,
    IN x3 double precision,
    IN y4 double precision,
    IN x4 double precision)
  RETURNS TABLE(distance double precision, the_geom geometry) AS
$BODY$ 

DECLARE
	node1 integer;
	node2 integer;
	node3 integer;
	node4 integer;

	path1 geometry;
	path2 geometry;
	path3 geometry;
	path4 geometry;
	
BEGIN
	-- Get nodes nearest the coordinates given
	SELECT tri_nearest(y1, x1) INTO node1;
	SELECT tri_nearest(y2, x2) INTO node2;
	SELECT tri_nearest(y3, x3) INTO node3;
	SELECT tri_nearest(y4, x4) INTO node4;

	-- Get the routes
	RETURN QUERY SELECT r.distance, r.the_geom
		FROM tri_route_1(node1, node2) r;

	RETURN QUERY SELECT r.distance, r.the_geom
		FROM tri_route_1(node2, node3) r;

	RETURN QUERY SELECT r.distance, r.the_geom
		FROM tri_route_1(node3, node4) r;

	RETURN QUERY SELECT r.distance, r.the_geom
		FROM tri_route_1(node4, node1) r;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION trifecta_diamond_route(double precision, double precision, double precision, double precision, double precision, double precision, double precision, double precision)
  OWNER TO anthony;
