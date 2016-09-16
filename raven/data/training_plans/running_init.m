% Training Plan Initialization
% © Trifecta Labs

% Helper function for initilizating the training plan generators.

% Creates a very easy, rudimentary training plan with a psuedorandom 
% element. 

% The training plan is then made more difficult until it reaches a
% difficulty appropriate to the users' level. 

function training_plan = running_init(lvl, mass, macro_varience)
    % Define the number of short, average, and long activities to generate
    short = macro_varience(1);
    avg = macro_varience(2);
    acts = sum(macro_varience);
    training_plan = zeros(acts,3);
    % Create psuedorandom activities
    for i = 1:acts
       if (short - i >= 0)
           training_plan(i,:) = [1 30+randn*sqrt(5) 25+randn*sqrt(5)];
       elseif (short + avg - i >= 0)
           training_plan(i,:) = [1 60+randn*sqrt(10) 60+randn*sqrt(10)];
       else
           training_plan(i,:) = [1 120+randn*sqrt(20) 120+randn*sqrt(20)];
       end
    end
    % Adjust the generated activities to match the users' level
    for j = 1:acts
        l = 0;
        while (l < lvl)
            l = running_level(training_plan(j,:), mass);
            training_plan(j,1) = training_plan(j,1) + 0.25;
        end
    end
end
