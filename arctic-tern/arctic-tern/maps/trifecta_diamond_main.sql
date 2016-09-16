-- Function: tri_triangle_route(double precision, double precision, double precision, double precision, double precision, double precision)

--DROP FUNCTION trifecta_diamond_main(double precision, double precision, double precision, double precision, double precision, double precision, double precision, double precision);

CREATE TYPE trifecta_route AS (distance double precision, route varchar(2000));

CREATE OR REPLACE FUNCTION trifecta_diamond_main(
    y1 double precision,
    x1 double precision,
    y2 double precision,
    x2 double precision,
    y3 double precision,
    x3 double precision,
    y4 double precision,
    x4 double precision)
  RETURNS trifecta_route AS
$BODY$

DECLARE
	res trifecta_route;
BEGIN

	SELECT sum(r.distance), ST_AsText(ST_UNION(r.the_geom))
	INTO res.distance, res.route 
	FROM trifecta_diamond_route(y1,x1,y2,x2,y3,x3,y4,x4) r;

	RETURN res;

END
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION trifecta_diamond_main(double precision, double precision, double precision, double precision, double precision, double precision, double precision, double precision)
  OWNER TO anthony;
