%calculate power required to complete an activity
%https://strava.zendesk.com/entries/20959332-Power-Calculations
function p = cycling_power(x, user_traits)
  mass = user_traits(2);
  c_rr = user_traits(3);
  c_d = user_traits(4);
  d = x(1);
  t = x(2);
  e = x(3);
  v = (d*1000)/(t*60);
  theta = asin(e/(d*1000/3));
  %https://en.wikipedia.org/wiki/Normal_force
  N_hill = cos(theta) * mass * 9.8;
  N_flat = mass * 9.8;
  %http://en.wikipedia.org/wiki/Rolling_resistance
  p_rr_hill = c_rr * N_hill * v;
  p_rr_flat = c_rr * N_flat * v;
  %https://en.wikipedia.org/wiki/Density_of_air
  ro = 1.255;
  %assuming frontal surface area
  a = 0.5;
  %http://en.wikipedia.org/wiki/Drag_(physics)
  p_wind = 0.5 * ro * v^3 * c_d * a;
  p_g = mass * 9.8 * sin(theta) * v;
  p_up = p_rr_hill + p_wind + p_g;
  p_flat = p_rr_flat + p_wind;
  p_down = p_rr_hill + p_wind - p_g;
  p = (p_up + p_flat + p_down)/3;
end