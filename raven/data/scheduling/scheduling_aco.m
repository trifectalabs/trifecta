% Scheduling ACO
% Based on ACO by Haupt & Haupt, 2003
% © Trifecta Labs

function [best_sched, best_score, it] = scheduling_aco(training_plan, calendar, obj)
    % Reset the random generator
    rng('default');
    rng('shuffle');
    
    buckets = scheduling_BucketGenerator(calendar);
    range = [1 size(buckets,1)];
    n = size(training_plan, 1);
    Npaths = range(2);       % number of options for each variable
    Nants = Npaths;     % number of ants = number of paths

    phmone = .1 * ones(Npaths, n); % initialized pheromones between cities

    maxit = 500; % max number of iterations
    a = 0.5; % alpha
    b = 0.5; % beta
    rr = 0.1; % decay rate
    Q = 4; % phermone quantity
    dbest = Inf; % shortest distance is initially infinite
    e = 5; % weight of elite path
    
    % Initialize ant paths
    for ia=1:Nants
       ants(ia,:) = randperm(Npaths, n);
    end
    
    
    for it=1:maxit
        % find the training plan for each ant
        % st is the current position
        % nxt contains list of next paths
        for ia=1:Nants
            % each ant holds a tabu list of paths already chosen to avoid
            % double booking bucket slots
            tabu=ones(Npaths,1);
            for iq=1:n
                % visibility is calculated by inversing the objective score
                % of each path the ant can, if an activity is longer than
                % the bucket size visibility returns 0 for that path
                visibility = vis(buckets,training_plan,iq,transpose(ants(ia,1:iq)));
                % prob is a vector of the probabilities that each path
                % could be chosen
                prob=((phmone(:,iq).^a).*(visibility.^b))./sum((phmone(:,iq).^a).*(visibility.^b));
                % the tabu list is used to set the probability of already
                % chosen paths to zero
                prob=prob.*tabu;
                % a random number is chosen between 0 and the total sum of
                % probabilities in prob. We cannot use 0 to 1 because the
                % tabu list removes some probabilities
                rpath=sum(prob).*rand(1,1);
                for iz=1:length(prob)
                    if rpath<sum(prob(1:iz))
                        newpath=iz; % next path to be taken
                        break
                    end % if
                end % iz
                ants(ia,iq)=newpath;
                tabu(newpath)=0;
            end % iq
        end % ia        
        % calculate the length of each tour and pheromone distribution
        phtemp=zeros(Npaths, n);
        for ic=1:Nants
            dist(ic)=0;
            for id=1:n
                antschedule = [transpose(1:n), training_plan(1:n,2)/15, transpose(ants(ic,1:iq))];
                dist(ic)=dist(ic)+scheduling_objective(antschedule,buckets);
                phtemp(ants(ic,id),id) = phtemp(ants(ic,id),id) + Q/dist(ic);
            end % id
        end % ic
        [dmin,ind] = min(dist);
        if dmin < dbest
            dbest = dmin;
            pbest = reshape(ants(ind,:),n,1);
        end % if
        % pheromone for elite path
        ph1 = zeros(Npaths, n);
        for id=1:n
            ph1(ants(ind,id),id) = Q/dmin;
        end % id
        % update pheromone trails
        phmone = (1-rr)*phmone + phtemp + e*ph1;
        dd(it,:) = [dbest dmin];
        %[it dmin dbest]
    end %it

    %set return values
    best_sched = [transpose(1:n) training_plan(1:n,2)/15 pbest];
    %best_plan = [transpose(distances(pbest(:,1))) transpose(times(pbest(:,2))) transpose(elevations(pbest(:,3)))];
    best_score = obj(best_sched, buckets);
end


