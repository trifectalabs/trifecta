drop FUNCTION tri_triangle_route (double precision, double precision,
					double precision, double precision,
					double precision, double precision);
CREATE OR REPLACE FUNCTION tri_triangle_route (y1 double precision, x1 double precision,
						y2 double precision, x2 double precision,
						y3 double precision, x3 double precision)
RETURNS RECORD AS $$

DECLARE
	res RECORD;
BEGIN

	SELECT INTO res 
	sum(r.distance), ST_AsText(ST_UNION(r.the_geom))
	FROM triangle_route(y1,x1,y2,x2,y3,x3) r;

	RETURN res;

END
$$ LANGUAGE plpgsql;