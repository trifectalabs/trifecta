% Bucket Generator 
% © Trifecta Labs

% Creates the respective buckets from a schedule.
% A bucket is defined as a time period in a calendar
% that is free and available for an activity to be scheduled.

% Only considers free time between 6am and 9pm, and outside of
% normal working hours of 9am - 5pm.

function buckets = BucketGenerator(calendar)
    startTimes = [];
    endTimes = [];
    % valid windows define the range from 6:00 - 21:00 daily
    validWindows = [24 84; 120 180; 216 276; 312 372; 408 468; 504 564; 600 660;...
    696 756; 792 852; 888 948; 984 1044; 1080 1140; 1176 1236; 1272 1332];
    j = 1;
    r = calendar(j);
    r2 = calendar(j+1);
    if (r == 0)
        startTimes = [startTimes 1];
    end
    % For each 15 minute window (1344 total in 2 weeks)
    while (j < 1342)
        % Iterate over the busy/free time until there is a switch (busy <-> free)
        while (r == r2 && j < 1342)
            j = j + 1;
            r = calendar(j);
            r2 = calendar(j+1); 
        end
        % If we went from a free time to busy time, mark it as a end of a bucket
        if (r == 0 && r2 == 1)
            endTimes = [endTimes j];
        % if we went from a busy time to a free time, mark it as a start of a bucket
        elseif (r == 1 && r2 == 0)
            startTimes = [startTimes j+1];
        end
        r = calendar(j+1);
        r2 = calendar(j+2); 
    end
    % Create the buckets based on start times, end times, and durations
    buckets = [transpose(startTimes) transpose(endTimes) transpose(endTimes-startTimes+1)];
    % Sort the buckets with respect to the 2 week period
    buckets = sortrows(buckets, -3);
end
