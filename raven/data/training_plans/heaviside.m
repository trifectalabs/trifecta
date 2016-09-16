function y = heaviside(X)
  %heaviside step function
  he = zeros(size(X));
  he(X > 0) = 1;
  he(X == 0) = .5;
  y = he .* X;
end
