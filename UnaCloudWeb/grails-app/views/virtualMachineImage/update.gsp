<html>
   <head>
      <meta name="layout" content="main"/>
   </head>
<body>
	<section class="content-header">
        <h1>
            Update Image Files
        </h1>
        <ol class="breadcrumb">
            <li><a href="${createLink(uri: '/', absolute: true)}"><i class="fa fa-dashboard"></i> Home</a></li>
            <li><a href="${createLink(uri: '/services/image/list', absolute: true)}"><i class="fa fa-th-list"></i> Images</a></li>
            <li class="active">Update Image</li>
        </ol>
    </section>    
    <section class="content"> 
    	<div class="row">
             <div class=" col-xs-12">   
            		<div id="label-message"></div>  
             		<g:if test="${flash.message && flash.message!=""}">
	          		<g:if test="${flash.type=="success"}">
	          			<div class="alert alert-success"><i class="fa fa-check"></i>
	          		</g:if>  
	          		<g:elseif test="${flash.type=="info"}">
	          			<div class="alert alert-info"><i class="fa fa-info"></i>
	          		</g:elseif> 
	          		<g:elseif test="${flash.type=="warning"}">
	          			<div class="alert alert-warning"><i class="fa fa-warning"></i>
	          		</g:elseif>             			
	          		<g:else>
	          			<div class="alert alert-danger"><i class="fa fa-ban"></i>
	          		</g:else> 	
			   		&nbsp;&nbsp;&nbsp;${flash.message}</div>
					</g:if>                		     
            		<div class="box box-primary">     	
            			<div class="box-header">
            				<h5 class="box-title">Update your image</h5> 							    			
            			</div>		
                 	<!-- form start -->
                 	<form id="form-change" action="${createLink(uri: '/services/image/update/save', absolute: true)}" enctype="multipart/form-data" role="form">
                     	 <div class="box-body">	                     		
            				 <p class="help-block">Upload the new files.</p> 
            				 <input id="id" name="id" type="hidden" value="${id}">                        	           
	                         <div class="form-group">
	                             <label>Image File input</label>
	                             <input id="files" type="file" name="files" multiple>
	                         </div>
	                     </div><!-- /.box-body -->
	                     <div class="box-footer"> 			
	                         <a id="button-submit" class="btn btn-primary" style="cursor:pointer">Submit</a>
	                     </div>
                	</form>
             	</div>
            </div><!-- /.box -->            
        </div>     	
	</section><!-- /.content -->     
	<asset:javascript src="pages/images.js" />
    <script>$(document).ready(function(){changeUploadImage();});</script>
</body>