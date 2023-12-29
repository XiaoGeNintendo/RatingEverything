<html>

<head>
    <title>Rating Graph of ${username} in course ${subjectname}</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta charset="UTF-8">
    <script src="
    https://cdn.jsdelivr.net/npm/jquery@3.7.1/dist/jquery.min.js
    "></script>
    <script src="
    https://cdn.jsdelivr.net/npm/fomantic-ui@2.9.3/dist/semantic.min.js
    "></script>
    <link href="
    https://cdn.jsdelivr.net/npm/fomantic-ui@2.9.3/dist/semantic.min.css
    " rel="stylesheet">
    <script src="
    https://cdn.jsdelivr.net/npm/cytoscape@3.28.0/dist/cytoscape.min.js
    "></script>
    <script src="https://code.highcharts.com/highcharts.js"></script>
    <script src="https://code.highcharts.com/modules/series-label.js"></script>
    <script src="https://code.highcharts.com/modules/exporting.js"></script>
    <script src="https://code.highcharts.com/modules/export-data.js"></script>
    <script src="https://code.highcharts.com/modules/accessibility.js"></script>
</head>

<body>
<div class="ui container" id="main" style="margin-top: 20px; margin-bottom: 20px;">

    <div id="content" style="margin-bottom: 20px; margin-top: 20px;">
        <div class="ui clearing segment toplevel">
            <h2 class="ui header">
                ${master.getRank(rating)}
                <b id="header"  style="color: #${master.getRankColor(rating)}">
                    ${username}</b>
            </h2>
            <h4 class="ui header">
                ${subjectname} Rating: ${rating}
            </h4>

            See also in: <a href="RANK.html">RANK</a>
            <#list otherSubjects as subject2>
                <a href="../${subject2}/${username}.html"> ${subject2} </a>
            </#list>
        </div>

        <div class="ui divider"></div>


        <div class="ui segment toplevel">
            <h3>Rating Graph</h3>
            <div id="ratingGraph">

            </div>
        </div>

        <div class="ui segment toplevel">
            <h3>Contest Information</h3>
            <table class="ui celled table">
                <thead>
                <tr><th>Name</th>
                    <th>Score</th>
                    <th>Rank</th>
                    <th>Delta</th>
                    <th>Rating</th>
                    <th>Title</th>
                </tr></thead>
                <tbody>

                <tr>
                    <td>Initial</td>
                    <td>-</td>
                    <td>-/-</td>
                    <td>-</td>
                    <td>1,500</td>
                    <td><span style="color:#${master.getRankColor(1500)};">${master.getRank(1500)}</span></td>
                </tr>

                <#assign rating=1500>
                <#list ratingChanges as change>
                    <#assign rating+=change.delta>
                    <tr>
                        <td>${change.name}</td>
                        <td>${change.point}</td>
                        <td>${change.rank}/${change.totalParticipants}</td>
                        <td>${change.getSignedDelta()}</td>
                        <td>${rating-change.delta}→${rating}</td>
                        <td><span style="color:#${master.getRankColor(rating)};">${master.getRank(rating)}</span></td>
                    </tr>
                </#list>
                </tbody>
            </table>
        </div>
    </div>
</div>

<script>
    Highcharts.chart('ratingGraph', {
        chart: {
            zoomType: "x",
            type: 'spline',
            scrollablePlotArea: {
                minWidth: 600,
                scrollPositionX: 1
            }
        },

        tooltip: {
            formatter: function() {
                return "Contest #"+this.x+"<br/><b>"+this.point.description+"</b><br/>Rating:"+this.y;
            }
        },

        title: {
            text: 'Rating graph of ${username} in course ${subjectname}',
            align: 'left'
        },
        yAxis: {
            title: {
                text: 'Rating'
            },
            minorGridLineWidth: 0,
            gridLineWidth: 0,
            alternateGridColor: null,
            plotBands: ${ratingRegionText}
        },
        series: [{
            name: 'Rating',
            data: ${ratingHistory},

        }],
        navigation: {
            menuItemStyle: {
                fontSize: '10px'
            }
        }
    });

        //play animation
        $('.ui.segment .item').transition('hide')
        $('.ui.segment.toplevel')
            .transition('hide')
            .transition({
                animation: 'scale in',
                reverse: 'auto', // default setting
                interval: 200,
                onComplete: function () {
                    // console.log(this)
                    $(this).find('.item')
                        .transition({
                            animation: 'scale in',
                            reverse: 'auto',
                            interval: 100
                        });

                    $('.ui.sticky').sticky({ context: '#content' });
                }
            });


        $('#time').popup({ on: 'focus' });
</script>
</body>

</html>