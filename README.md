# Rating Everything
This is a playground & toolbox for [Codeforces'](https://codeforces.com) Rating System.

What can it do?
- Add anything that you want to generate a rating (w/ support for multi-aspects like Algorithm+Heuristic in Atcoder)
- Calculate ratings and rankings
- Nothing more

# How to use

Put your contest result csv in `contests/` then do like the example:
```
ICSIE1 <-- ID. NO SPACES!
false <-- Whether it has been rated. Just type false 
ICSIE Homework 1 on 2023/10/08 <-- Description. Write anything you want.
CS <-- Category. Contests in the same category accounts for the same rating
name,point,A,B,C,D,Solved,Score,Penalties,Δ <--Start from here is your csv file. Column 1 MUST be 'name'(NO SPACES). Column 2 MUST be 'point'. 'point' is the ONLY standard for ranking. Other columns will be treated as decorations.
231502002,400.9915,100 (+1),100 (+1),100 (+1),100 (+1),4,400,850,+179
231502004,400.98666,100 (+1),100 (+2),100 (+2),100 (+1),4,400,1334,+138
231502013,400.98462,100 (+1),100 (+1),100 (+1),100 (+1),4,400,1538,+114
```
Then build and run in a terminal(no binary files at the moment). Type `help` and you can see the commands.

For example:
- `contest Homework1`
- `pend Homework1`
- `user XGN`
- `rank CS`

# Note
Please pay attention to the upper/lower cases of your input.

Make sure there's **NO SPACE** in username/contest ID.

You can rewrite the rank function in `Main.kt::getRank()`