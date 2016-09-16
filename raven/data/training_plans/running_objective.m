% Objective function to evaluate a training plan

% user_traits = [height mass c_rr c_d]

% Returns the fitness of a training plan

function fitness = running_objective(training_plan, user_fitness, mass)
    length = 14; %length of the training plan in days

    %calculate initial fitness based on efforts from each activity
    fitness = 0;
    for i=1:size(training_plan)
        fitness = fitness + (W(training_plan(i,:)));
    end
    %penalize based on constraints
    fitness = fitness - H(training_plan) - Q(training_plan, user_fitness) - V(training_plan);
    %minimum fitness of zero
    fitness = heaviside(fitness);
    
    function y = heaviside(X)
      %heaviside step function
      he = zeros(size(X));
      he(X > 0) = 1;
      he(X == 0) = .5;
      y = he .* X;
    end

    %effort for an activity
    function w = W(x)
        %short activity
        if (x(2) >= 30 && x(2) < 60)
            w=120;
        %average activity
        elseif (x(2) >= 60 && x(2) <= 120)
            w=250;
        %long activity
        else
            w=2.75*x(2);
        end
    end

    %penalty for receovery days
    function h = H(X)
        %estimate total recovery time required
        recovery_time = 0;
        for j=1:size(X)
            recovery_time = recovery_time + (W(X(j,:)) / 200);
        end
        %penalty based on how much longer recovery time is than the length
        %of the training plan
        h = 500 * heaviside(recovery_time - length);
    end

    %penalty for feasibility
    function q = Q(X, user_fitness)
        total_levels = 0;
        for j=1:size(X)
           %estimate fitness level required to complete an activity
           l = running_level(X(j,:), mass);
           upper_limit = user_fitness + 1;
           if user_fitness < 5
               lower_limit = 1;
           else
               lower_limit = user_fitness - 4;
           end
           %above allowed range
           if l > upper_limit
               lvl_penalty = l - upper_limit;
           %below allowed range
           elseif l < lower_limit
               lvl_penalty = lower_limit - l;
           %acceptable range
           else
               lvl_penalty = 0;
           end
           total_levels = total_levels + lvl_penalty;
        end
        %penalize based on the number of levels outside of the range
        q = heaviside(50 * total_levels);
    end

    %penalty for activity length variance
    function v = V(training_plan)
        short = 0;
        average = 0;
        long = 0;
        penalty = 0;
        %calculate training plan activity length variance
        row = size(training_plan, 1);
        for j = 1:row
            activity = training_plan(j,:);
            duration = activity(2);
            if duration >= 30 && duration < 60
                short = short + 1;
            elseif duration >= 60 && duration <= 120
                average = average + 1;
            elseif duration > 120
                long = long + 1;
            end
        end
        %penalize based on percent outside of allowed range
        short_p = short/row;
        if (short_p >= 0.35)
            penalty = penalty + (short_p - 0.35)*7500;
        elseif (short_p <= 0.15)
            penalty = penalty + (0.15 - short_p)*7500;
        end
        avg_p = average/row;
        if (avg_p >= 0.6)
            penalty = penalty + (avg_p - 0.6)*7500;
        elseif (avg_p <= 0.4)
            penalty = penalty + (0.4 - avg_p)*7500;
        end
        long_p = long/row;
        if (long_p >= 0.37)
            penalty = penalty + (long_p - 0.35)*7500;
        elseif (long_p <= 0.15)
            penalty = penalty + (0.15 - long_p)*7500;
        end
        v = penalty;
    end
end