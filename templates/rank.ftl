<html>

<head>
    <title>Rank Graph course ${subjectname}</title>
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
                Ranking of ${subjectname}
            </h2>
        </div>

        <div class="ui divider"></div>

        <div class="ui segment toplevel">
            <h3>Participants</h3>
            <table class="ui celled table">
                <thead>
                <tr>
                    <th>Rank</th>
                    <th>Name</th>
                    <th>Rating</th>
                    <th>Contest Count</th>
                </tr></thead>
                <tbody>


                <#list rank as row>
                    <tr>
                        <td>${row.rank}</td>
                        <td><a href="${row.name}.html" style="color:#${master.getRankColor(row.rating)};">${row.name}</a></td>
                        <td>${row.rating}</td>
                        <td>${row.contestCount}</td>
                    </tr>
                </#list>
                </tbody>
            </table>
        </div>
    </div>
</div>

<script>

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