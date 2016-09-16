% Scheduling Initialization
% © Trifecta Labs

% Helper function for initilizating the schedulers.

% Creates a very rudimentary schedule, by iterating
% over the training plan and assigning each activity 
% to the first bucket it fits in 

function scheduled_TP = scheduling_init(training_plan, buckets)
    % Sort the training plan based on duration
    sortedTP = sortrows(training_plan, -2);
    scheduled_TP = zeros(size(training_plan,1), size(training_plan, 2));
    % For each activity in the sorted list
    for j = 1:size(sortedTP, 1)
        act = sortedTP(j,:);
        dur = ceil(act(2)/15);
        % Find the first bucket that it fits in
        for b = j:size(buckets, 1)
            bucket = buckets(b,:);
            % If it fits in the bucket, add it and continue
            if (bucket(2) - bucket(1) >= dur)
                scheduled_TP(j,:) = [j dur b];
                break;
            end
        end
    end
end
