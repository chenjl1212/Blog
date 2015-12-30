var bid=request("bid");

$(document).ready(function() {
	
	BlogManager.getBlog(bid, function(blog) {
		if(blog==null) {
			location.href="urlError.html";
			return;
		}
		$("#edit-blog-date").text(blog.date.format(DATE_HOUR_MINUTE_SECOND_FORMAT));
		$("#edit-blog-title").val(blog.title);
		$("#edit-blog-content").summernote({
			height: 700
		}).summernote("code", blog.content);
	});

	$("#edit-blog-clear").click(function() {
		$.messager.confirm("Tip", "Confirm to clear title and content of this blog article?", function() {
            $("#edit-blog-title").val("");
            $("#edit-blog-content").summernote("code", "");
        });
	});

	$("#edit-blog-submit").click(function() {
		var title=$("#edit-blog-title").val();
    	var content=$("#edit-blog-content").summernote("code");
    	if(title==""||content=="") {
    		$.messager.popup("Input title and content!");
    		return;
    	}
    	BlogManager.modifyBlog(bid, title, content, function() {
    		$.messager.popup("Mofify this blog successfully!");
    		setTimeout(function() {
				location.href="manageBlogs.html";
			}, 1000);
    	});
	});
});