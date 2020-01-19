package cz.iwitrag.greencore.premium.handlers;

public class HandlerFactory {

    private HandlerFactory() {}

    public static Handler getHandler(String service) {
        if (service.equalsIgnoreCase("vip"))
            return new VipHandler();
        if (service.equalsIgnoreCase("vipplus"))
            return new VipPlusHandler();
        else
            return new NotImplementedHandler();
    }

}
