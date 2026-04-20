#import <Foundation/Foundation.h>
#import "NativeCallProxy.h"

@implementation FrameworkLibAPI

id<NativeCallsProtocol> api = NULL;
+(void) registerAPIforNativeCalls:(id<NativeCallsProtocol>) aApi
{
    api = aApi;
}

+(void) unregisterAPIforNativeCalls
{
    api = NULL;
}

@end

extern "C"
{
    void sendMessageToMobileApp(const char* message)
    {
        if (api != NULL) {
            [api sendMessageToMobileApp:[NSString stringWithUTF8String:message]];
        }
    }
}
