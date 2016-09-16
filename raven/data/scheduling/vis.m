% Visibility
% © Trifecta Labs

% Helper function for calculating visibility

% returns a vector of the visibility of each path
function visibility = vis(buckets, training_plan, index, bucketindex)
%VIS Summary of this function goes here
%   Detailed explanation goes here
    schedule = [transpose(1:index), training_plan(1:index,2)/15, bucketindex];
    bucketsize = buckets(:,3,:);
    visibility = zeros(size(bucketsize,1),1);
    currentactivity = training_plan(index,:);
    for i = 1:size(bucketsize,1)
        if (bucketsize(i) < currentactivity(2)/15)
            visibility(i) = 0;
        else
            visibility(i) = 1/(1+vis_scheduling_objective(schedule,buckets));
        end  
    end
    

end

