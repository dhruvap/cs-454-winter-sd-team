<%@ page import="com.csula.Data" %>
<%@ page import="java.util.List" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>


<head>

    <link rel="stylesheet" href="http://code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css">
    <script src="http://code.jquery.com/jquery-1.10.2.js"></script>
    <script src="http://code.jquery.com/ui/1.11.4/jquery-ui.js"></script>
    <link rel="stylesheet" href="https://jqueryui.com/resources/demos/style.css">
    <title></title>
</head>
<body>
<script>
    $(function() {
        var availableTags = [

        ];
        $( "#tags" ).autocomplete({
            source: availableTags
        });
    });


    $('.previewLink').on('focus', preview())

    function preview(link){
        $('#preview').html('<iframe src="'+link+'" height="600px" width="600px"> </iframe>');
    }

    function unpreview(){
        $('#preview').html('');
    }

    $(document).ready(function(){
        $("#tags").keyup(function(){
            $.ajax({
                type: "GET",
                url: "Suggest.html",
                data:'q='+$(this).val(),
                beforeSend: function(){
                   // $("#search-box").css("background","#FFF url(LoaderIcon.gif) no-repeat 165px");
                },
                success: function(data){
                    var res = JSON.parse(data);
                    var html = "";
                    var tags = [];
                    for(var key in  res){
                        key = res[key];
                        html+="<div onfocus='$(\'#search-box\').val("+key.term+")' >"+key.term+"</div><br/>";
                        tags.push(key.term);


                    }

                    $( "#tags" ).autocomplete({
                        source: tags
                    });

                   // $("#suggesstion-box").show();
                   // $("#suggesstion-box").html(html);
                   // $("#search-box").css("background","#FFF");
                }
            });
        });
    });
    //To select country name
    function selectCountry(val) {
        $("#search-box").val(val);
        $("#suggesstion-box").hide();
    }
</script>

<form method="POST" action="Search.html">
    <div class="ui-widget">
        <label for="tags">Query: </label>
        <input id="tags" name="q">
    </div>
    <input type="submit" value="search"/>

</form>
<div id="preview" style="position: absolute; left :600px; top:10px; height:700px; width:100%;">

</div>
<%
    List<Data.SearchResult> res = (List<Data.SearchResult>) request.getAttribute("res");
    if(res != null){
        %>
        <table>

<%
        for(Data.SearchResult e : res){
        %>
            <tr><td>
                <a class="previewLink"  onblur="unpreview()" href="#"><%=e.link%></a> ( <%=e.score%> )
                <br/>
                <button onclick="preview('<%=e.link%>')">Preview</button>
                <button onclick="unpreview()">Close Preview</button><br/>
                Other Tags Leading this search :<br/>
                <div style="width: 500px;display :block;">
                <%
                    for(String tag : e.otherTags){
                        %>
                    <span style=";color:blue;text-decoration:underline;padding:2px; margin-right:5px;"><%=tag%></span>
                <%
                    }
                %>
                </div>
            </td></tr><br/>
            <%

        }
        %>
        </table>
<%
    }
%>
</body>
</html>
