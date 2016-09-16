%calculate power required to complete a run
function p = running_power(x, mass)
  d = x(1);
  t = x(2);
  e = x(3);
  v = (d*1000)/(t*60);
  theta = asin(e/(d*1000/3));
  %http://sprott.physics.wisc.edu/technote/walkrun.htm
  p_r = mass * 9.8 * v / 4;
  p_g = mass * 9.8 * sin(theta) * v;
  p_up = p_r + p_g;
  p_flat = p_r;
  p_down = p_r - p_g;
  p = (p_up + p_flat + p_down)/3;
end