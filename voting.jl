# Voting simulation for downvoting
using Random
using Distributions
using DataFrames
using CSV

# Definition of quality
# N(μ = 0, σ = 1)
# MVN(M = [0,..,0], Σ = positiv definite Cov Matrix)
quality_dimensions = 3
M = [0 for i = 1:quality_dimensions]
sigma_col_vec = [0.2 for i = 1:quality_dimensions]
sigma_row_vec = [0.5 for i = 1:quality_dimensions]
Σ = sigma_col_vec * sigma_row_vec'

for i = 1:quality_dimensions
    Σ[i, i] = 1
end


quality_distribution = Distributions.MvNormal(M, Σ)


mutable struct Post
    quality::Vector{Float64}
    votes::Int64
    timestamp::Int64
    score::Float64
end

function Post(rng, timestamp)
    Post(rand(rng, quality_distribution), 0, timestamp, 0.0)
end



max_iterations = 30
new_content_probability = 0.1
start_users = 1_00
new_user_probability = 0
vote_probability_scale = 10
frontpagesize = 10


mutable struct User
    quality_perception::Vector{Float64}
    vote_probability::Float64
end

function User(rng)
    User(rand(rng, quality_distribution), rand(rng)/vote_probability_scale)
end



function setup!(rng, all_posts, all_users)
    append!(all_posts, [Post(rng, 0) for i = 1:frontpagesize])
    append!(all_users, [User(rng) for i = 1:start_users])
end


function scoring(post, time)
    - post.votes * (time - post.timestamp)
end


function go!(rng, it, all_posts, all_users)

    if (rand(rng) < new_content_probability)
        push!(all_posts, Post(rng, it))
    end

    if (rand(rng) < new_user_probability)
        push!(all_users, User(rng))
    end

    for post in all_posts
        post.score = scoring!(post, it)
    end

    votes_idx = partialsortperm(all_posts, 1:frontpagesize, by= x->x.score)
    window = all_posts[votes_idx]

    for user in all_users
            if rand(rng) < user.vote_probability
                # TODO: empiricial validation of this distribution
                # x <- runif(10000) * runif(10000)
                # hist(x)
                idx = Int64(ceil( rand(rng) * rand(rng) * frontpagesize ))

                view_post = window[idx]
                if sum(view_post.quality .* user.quality_perception) < 0
                    view_post.votes += 1
                end

            end
    end


    result_row = DataFrame(
        user_count = length(all_users),
        post_count = length(all_posts),
        votes = sum(x -> x.votes, all_posts))
    result_row
end



function simulation(rng)

    all_posts = Post[]
    all_users = User[]
    setup!(rng, all_posts, all_users)
    result = DataFrame()
    for it = 1:max_iterations
        res_row = go!(rng, it, all_posts, all_users)
        result = append!(result, res_row)
    end
    result
end



df = simulation(MersenneTwister(1))

#CSV.write(df, "test.csv")


# TODO: State_object
# Traits for different scoring (see scala)
# config_object
# report state more meaningful
# was ist quality?
# - random values für user
# - population dependent :(
# abs for all user quality perceptions
#   total_quality len(quality)

# alternativ 1-dim quality + noise
#
