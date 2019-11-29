struct Vote
    value::Float64
end


myvotes = [Vote(i*j*1.3) for i in 1:100, j in 1:100]

rand(100000)

for i in 1:100
    println(myvotes[i])
end
