# Simulations of news aggregator voting systems

A simple simulation of different voting systems and user behavior:

- Content submission of predefined quality between `0` and `1`
- A frontpage defined by a scoring formula
- Users with different expectations and voting behavior on the content of the frontpage

The plots show the quality on the X-axis and votes on the y-axis.

## Hacker News

![](correlation-data-hn.png)

Scoring Formula:
```scala
val gravity = 1.8
val base = upvotes + 1
val score = (if (base > 0) Math.pow(base, 0.8) else base) / Math.pow(age + 1, gravity)
```

Observations:
- Better quality content has the chance to reach higher scores
- There are no false positives (low quality content with high score)
- There are many false negatives (high quality content with low score)
- There are outliers with very high scores
- The variation of scores is higher on higher quality content

## Hacker News Normalized by Views

![](correlation-data-hn-normalized.png)

Scoring Formula:
```scala
val gravity = 1.8
val base = upvotes + 1
val score = (if (base > 0) Math.pow(base, 0.8) else base) / Math.pow(age + 1, gravity) / views
```

Observations:
- The formula stayed the same and keeps its time-based decay characteristics
- There are no more false negatives (high quality content with low score)

## Only Downvotes

![](correlation-data-onlydownvote.png)

Scoring Formula:
```scala
val score = -(downvotes+1) * age
```

Observations:
- There are no false positives (low quality content with high score)
- There are no false negatives (high quality content with low score)
- The variation of scores is higher on lower quality content

## Upvotes with Views as Downvotes

Scoring Formula:
```scala
val score = (upvotes-views-1) * age
```

Observations:
- There are no false positives (low quality content with high score)
- There are no false negatives (high quality content with low score)
- The variation of scores is higher on lower quality content

![](correlation-data-removedownvote.png)



## Running the Simulation
Set the preferred ranking algorithm at the top of `scoring-simulation.scala`:
```scala
val scoring: Scoring = RemoveDownvote
```

Then run:
```
scala scoring-simulation.scala > data
```

Plot the result using:
```
gnuplot -e "set terminal png size 1920,1080; plot 'data' using 1:2" > data.png
```


## Related articles
- https://felix.unote.io/hacker-news-scores (analysis of scoring inconsistencies on Hacker News)


## Contact
If you'd like to talk about this topic, please contact me: felix.dietze@rwth-aachen.de
