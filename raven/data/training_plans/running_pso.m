% Training Plan PSO
% Based on PSO by Haupt & Haupt, 2003
% © Trifecta Labs

% Takes a parameter of user_fitness_data which has the following format
% [Umax_climb user_fitness]

% and user_traits which has the following format
% [height mass c_rr c_d]

% and user_prefs which has the following format
% [num_acts     0               0
%  pct_short    short_low_range short_high_range
%  pct_avg      avg_low_range   avg_high_range
%  pct_long     long_low_range  long_high_range]

% Takes a function obj which is the objective function

function [best_plan, best_score, iter] = running_pso(user_fitness_data, user_traits, user_prefs)
    %Initialization
    % Range has the following format for short (1-3), avg (4-6), and long (7-9):
        % [Distance_min Distance_max]
        % [Time_min Time_max]
        % [Elevation_min Elevation_max]
    range = [user_prefs(2,2)/60*7 user_prefs(2,3)/60*18;...
             user_prefs(2,2) user_prefs(2,3);...
             0 75;...
             
             user_prefs(3,2)/60*7 user_prefs(3,3)/60*14;...
             user_prefs(3,2) user_prefs(3,3);...
             50 250;...
             
             user_prefs(4,2)/60*7 user_prefs(4,3)/60*10;...
             user_prefs(4,2) user_prefs(4,3);...
             100 user_fitness_data(1)*1.25];
        
    mass = user_traits(2);
    user_fitness = user_fitness_data(2); % user's fitness level
    n=user_prefs(1,1); % number of activities
    macro_varience = [...
        floor(n*user_prefs(2,1))...   % Number of short activities
        ceil(n*user_prefs(3,1))...    % Number of average activities
        floor(n*user_prefs(4,1))];    % Number of long activities
    
    popsize = 15;   % Size of the swarm
    npar = n*3;     % Dimension of the problem
    maxit = 1000;   % Maximum number of iterations
    c1 = 1.4944;    % cognitive parameter
    c2 = 1.4944;    % social parameter
    w = 0.792;      % inertia
    
    % random population of training plans
    par = [];
    for i = 1:popsize
        par = [par; reshape(running_init(user_fitness,mass,macro_varience),1,npar)];
    end
    vel = rand(popsize,npar); % random velocities
    score = [];
    % scores of each particle
    for i = 1:popsize
       score = [score; running_objective(reshape(par(i,:),n,3),user_fitness,mass)]; 
    end

    maxc(1) = max(score);   % max score
    meanc(1) = mean(score); % mean score
    globalmax = maxc(1);    % initialize global maximum
    localpar = par;         % location of local maxima
    localscore = score;     % cost of local maxima
    
    % Finding best particle in initial population
    [globalscore,indx] = max(score);
    globalpar = par(indx,:);

    % Start iterations
    iter = 0;
    while iter < maxit
        iter = iter + 1;
        % update velocity = vel
        r1 = rand(popsize,npar); % random numbers
        r2 = rand(popsize,npar); % random numbers
        vel = (w*vel + c1 *r1.*(localpar-par) + c2*r2.*(ones(popsize,1)*globalpar-par));
        % update particle positions
        par = par + vel;
        
        % Make sure all particles are still within the distance search space
        distance_par = par(:,1:n);
        short_distance_par = distance_par(:,1:macro_varience(1));
        avg_distance_par = distance_par(:,macro_varience(1)+1:macro_varience(1)+macro_varience(2));
        long_distance_par = distance_par(:,macro_varience(1)+macro_varience(2)+1:macro_varience(1)+macro_varience(2)+macro_varience(3));
        short_distance_overlimit = short_distance_par<=range(1,2);
        short_distance_underlimit = short_distance_par>=range(1,1);
        avg_distance_overlimit = avg_distance_par<=range(4,2);
        avg_distance_underlimit = avg_distance_par>=range(4,1);
        long_distance_overlimit = long_distance_par<=range(7,2);
        long_distance_underlimit = long_distance_par>=range(7,1);
        short_distance_par = short_distance_par.*short_distance_overlimit + not(short_distance_overlimit)*range(1,2);
        short_distance_par = short_distance_par.*short_distance_underlimit + not(short_distance_underlimit)*range(1,1);
        avg_distance_par = avg_distance_par.*avg_distance_overlimit + not(avg_distance_overlimit)*range(4,2);
        avg_distance_par = avg_distance_par.*avg_distance_underlimit + not(avg_distance_underlimit)*range(4,1);
        long_distance_par = long_distance_par.*long_distance_overlimit + not(long_distance_overlimit)*range(7,2);
        long_distance_par = long_distance_par.*long_distance_underlimit + not(long_distance_underlimit)*range(7,1);
        distance_par = [short_distance_par avg_distance_par long_distance_par];
        % Make sure all particles are still within the time search space
        time_par = par(:,1+n:n*2);
        short_time_par = time_par(:,1:macro_varience(1));
        avg_time_par = time_par(:,macro_varience(1)+1:macro_varience(1)+macro_varience(2));
        long_time_par = time_par(:,macro_varience(1)+macro_varience(2)+1:macro_varience(1)+macro_varience(2)+macro_varience(3));
        short_time_overlimit = short_time_par<range(2,2);
        short_time_underlimit = short_time_par>=range(2,1);
        avg_time_overlimit = avg_time_par<=range(5,2);
        avg_time_underlimit = avg_time_par>=range(5,1);
        long_time_overlimit = long_time_par<=range(8,2);
        long_time_underlimit = long_time_par>range(8,1);
        short_time_par = short_time_par.*short_time_overlimit + not(short_time_overlimit)*range(2,2);
        short_time_par = short_time_par.*short_time_underlimit + not(short_time_underlimit)*range(2,1);
        avg_time_par = avg_time_par.*avg_time_overlimit + not(avg_time_overlimit)*range(5,2);
        avg_time_par = avg_time_par.*avg_time_underlimit + not(avg_time_underlimit)*range(5,1);
        long_time_par = long_time_par.*long_time_overlimit + not(long_time_overlimit)*range(8,2);
        long_time_par = long_time_par.*long_time_underlimit + not(long_time_underlimit)*range(8,1);
        time_par = [short_time_par avg_time_par long_time_par];
        % Make sure all particles are still within the elevation search space
        elevation_par = par(:,1+2*n:n*3);
        short_elevation_par = elevation_par(:,1:macro_varience(1));
        avg_elevation_par = elevation_par(:,macro_varience(1)+1:macro_varience(1)+macro_varience(2));
        long_elevation_par = elevation_par(:,macro_varience(1)+macro_varience(2)+1:macro_varience(1)+macro_varience(2)+macro_varience(3));
        short_elevation_overlimit = short_elevation_par<=range(3,2);
        short_elevation_underlimit = short_elevation_par>=range(3,1);
        avg_elevation_overlimit = avg_elevation_par<=range(6,2);
        avg_elevation_underlimit = avg_elevation_par>=range(6,1);
        long_elevation_overlimit = long_elevation_par<=range(9,2);
        long_elevation_underlimit = long_elevation_par>=range(9,1);
        short_elevation_par = short_elevation_par.*short_elevation_overlimit + not(short_elevation_overlimit)*range(3,2);
        short_elevation_par = short_elevation_par.*short_elevation_underlimit + not(short_elevation_underlimit)*range(3,1);
        avg_elevation_par = avg_elevation_par.*avg_elevation_overlimit + not(avg_elevation_overlimit)*range(6,2);
        avg_elevation_par = avg_elevation_par.*avg_elevation_underlimit + not(avg_elevation_underlimit)*range(6,1);
        long_elevation_par = long_elevation_par.*long_elevation_overlimit + not(long_elevation_overlimit)*range(9,2);
        long_elevation_par = long_elevation_par.*long_elevation_underlimit + not(long_elevation_underlimit)*range(9,1);
        elevation_par = [short_elevation_par avg_elevation_par long_elevation_par];
        % Build constrainted solution
        par = [distance_par time_par elevation_par];
        
        % Evaluate the new swarm
        for i = 1:popsize
            score(i) = running_objective(reshape(par(i,:),n,3),user_fitness,mass);
        end
        % Updating the best local position for each particle
        betterscore = score > localscore;
        localscore = localscore.*not(betterscore) + score.*betterscore;
        localpar(find(betterscore),:) = par(find(betterscore),:);
        % Updating index g
        [temp, t] = max(localscore);
        if temp > globalscore
            globalpar = par(t,:);
            globalscore = temp;
        end
        
        maxc(iter+1) = max(score); % min for this iteration
        globalmax(iter+1) = globalscore; % best max so far
        meanc(iter+1) = mean(score); % avg. cost for this iteration
        
        %Check termination criteria
        if (iter > 200)
            last_100 = max(globalmax(iter-200:iter-101));
            curr_100 = max(globalmax(iter-100:iter));
            pct_inc = (curr_100 - last_100)/last_100 * 100;
            if (pct_inc < 0.1)
                break;
            end
        end
    end
    
    %set return values
    best_plan = reshape(globalpar,n,3);
    best_score = globalmax(iter+1);
    
    % Uncomment to see algorithm graph
	figure(24)
	iters = 0:length(maxc)-1;
    plot(iters,maxc,iters,meanc,iters,globalmax,':');
	xlabel('generation');ylabel('score');
	text(0,maxc(1),'best');text(1,maxc(2),'population average')
	set(gcf,'color','w');
end
