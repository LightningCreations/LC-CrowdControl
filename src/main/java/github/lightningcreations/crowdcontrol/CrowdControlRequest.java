package github.lightningcreations.crowdcontrol;

public final class CrowdControlRequest {
    private final int id;
    private final String code;
    private final String viewer;
    private final CrowdControlRequestType type;
    public static final class Response{
        private final int id;
        private final CrowdControlResponseType status;
        private final String message;

        Response(int id, CrowdControlResponseType status, String message) {
            this.id = id;
            this.status = status;
            this.message = message;
        }

        public int getId() {
            return id;
        }

        public CrowdControlResponseType getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }

    public CrowdControlRequest(int id, String code, String viewer, CrowdControlRequestType type) {
        this.id = id;
        this.code = code;
        this.viewer = viewer;
        this.type = type;
    }

    public String getViewer() {
        return viewer;
    }

    public CrowdControlRequestType getType() {
        return type;
    }

    public String getCode() {
        return code;
    }

    public int getId() {
        return id;
    }

    public Response success(String message){
        return new Response(id,CrowdControlResponseType.Success,message);
    }

    public Response failure(String message){
        return new Response(id,CrowdControlResponseType.Failure,message);
    }

    public Response retry(String message){
        return new Response(id,CrowdControlResponseType.Retry,message);
    }

    public Response unavailable(String message){
        return new Response(id,CrowdControlResponseType.Unavailable,message);
    }

}
