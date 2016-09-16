% Objective function to evaluate scheduling a training plan

% scheduled_training_plan is a list of the activities with associated start_time
% scheduled_training_plan = [X_id X_duration X_start; ...]

% Returns the fitness of a scheduled training plan

function fitness = scheduling_objective(scheduled_training_plan, buckets)
    % The R function calcuates the cost of the scheduled activities based on the 
    % recovery function. The recovery function dictates that there should be a 
    % reasonable rest period in between activities to allow an athlete to recover.
    function r = R(eff, t)
        if t < eff/100
            r = (-3000000/(eff*eff)) * t^3 + (45000/eff)*t^2;
        else
            r = 8*t + (3/2)*eff - (8/100)*eff;
        end
    end

    % The W function calcuates the effort needed to complete the activity. 
    % This effort is used to determine how long of a rest period is optimal 
    % before scheduling the next activity.
    function w = W(x)
        if (x(2)*15 >= 30 && x(2)*15 < 60)
            w=120+randn(1)*15;
        elseif (x(2)*15 >= 60 && x(2)*15 <= 120)
            w=250+randn(1)*30;
        else
            w=2.75*x(2)*15;
        end
    end

    % The H function calculates the fitness value of the scheduled training plan
    % It is dependent on R and W
    function h = H(scheduled_training_plan)
        h = 0;
        for n = 1:size(scheduled_training_plan,1)-1
            act = scheduled_training_plan(n,:);
            effort = W(act);
            h = h + abs((R(effort, (effort/200)) - R(effort,(scheduled_training_plan(n+1,3)-act(3))/96)));
        end
    end

    % Sort the scheduled training plan to facilitate in calculating the recovery period
    sortedPlan = sortrows(scheduled_training_plan, 3);
    for p = 1:size(sortedPlan,1)
        sortedPlan(p,3) = buckets(sortedPlan(p,3),1);
    end
   
    % Calculate the fitness value 
    h = H(sortedPlan);
    fitness = h;
end
