-- Function: tri_nearest(double precision, double precision)

-- DROP FUNCTION tri_nearest(double precision, double precision);

CREATE OR REPLACE FUNCTION tri_nearest(
    y1 double precision,
    x1 double precision)
  RETURNS integer AS
$BODY$ 

DECLARE
	node integer;
BEGIN
	SELECT n.id INTO node
		FROM ways_vertices_pgr n
		ORDER BY n.the_geom <-> ST_GeometryFromText('POINT('||x1||' '||y1||')',4326) 
		LIMIT 1;

	RETURN node;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION tri_nearest(double precision, double precision)
  OWNER TO anthony;
