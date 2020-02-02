package github.lightningcreations.crowdcontrol;

/**
 * Class representing a Request coming from the Crowd Control Server to perform some effect.
 *
 * The structure of this class is guaranteed to match the field layout of the CrowdControl TCP v1 Json structure (with an unspecified order),
 *  so it may be used for reflective serialization where the order is not necessarily maintained (such as json).
 */
public final class CrowdControlRequest {
    private final long id;
    private final String code;
    private final String viewer;
    private final byte type;

    /**
     * Represents a Response to some request. These structures cannot be constructed directly.
     *
     * Like {@link CrowdControlRequest}, this class matches the respective field structure of the Crowd Control TCP v1 Protocol.
     */
    public static final class Response{
        private final long id;
        private final byte status;
        private final String message;

        Response(long id, CrowdControlResponseType status, String message) {
            this.id = id;
            this.status = (byte)status.ordinal();
            this.message = message;
        }

        /**
         * Gets the id of this Response.
         */
        public int getId() {
            return (int)id;
        }

        /**
         * Gets the status field, as a CrowdControlResponseType enum.
         */
        public CrowdControlResponseType getStatus() {
            return CrowdControlResponseType.values()[status];
        }

        /**
         * Gets the response message for the Server.
         */
        public String getMessage() {
            return message;
        }
    }

    /**
     * Constructs a new Requeust from a given id, code, viewer, and type.
     *
     * You will not need this constructor, except while implementing a connector.
     */
    public CrowdControlRequest(int id, String code, String viewer, CrowdControlRequestType type) {
        this.id = id;
        this.code = code;
        this.viewer = viewer;
        this.type = (byte)type.ordinal();
    }

    /**
     * Gets the viewer field of this request.
     */
    public String getViewer() {
        return viewer;
    }

    /**
     * Gets the type of this request
     */
    public CrowdControlRequestType getType() {
        return CrowdControlRequestType.values()[type];
    }

    /**
     * Gets the "stable" code name for the effect.
     */
    public String getCode() {
        return code;
    }

    /**
     * Gets the unique id for this request
     */
    public int getId() {
        return (int)id; //Note: I would have made id a UUID
    }

    /**
     * Creates a response to this request, indicating success, with the given message
     */
    public Response success(String message){
        return new Response(id,CrowdControlResponseType.Success,message);
    }

    /**
     * Creates a response to this request, indicating failure, with the given message
     */
    public Response failure(String message){
        return new Response(id,CrowdControlResponseType.Failure,message);
    }

    /**
     * Creates a response to this request, indicating that the server should retry the effect, with the given message
     */
    public Response retry(String message){
        return new Response(id,CrowdControlResponseType.Retry,message);
    }

    /**
     * Creates a response to this request, indicating that the effect is unavailable, with the given message
     */
    public Response unavailable(String message){
        return new Response(id,CrowdControlResponseType.Unavailable,message);
    }
}
