require 'uri'
require 'sinatra/base'
require 'redis'
require 'json'
require 'json/add/core'
require 'redis/connection/synchrony'

module Sinatra
  module RedisCache
    module Helpers
      def cache key, expiration, &block
        cached = settings.redis.get key
        if cached.nil? and block_given?
          store_data key, expiration, block
        else
          cached = JSON.parse(cached)
          if Time.now < cached["__exp"]
            cached["data"]
          else
            store_data key, expiration, block
          end
        end
      end

      private 

      def store_data key, expiration, block
        result = block.call
        data = {
          data: result,
          __exp: expiration
        }
        settings.redis.set key, data.to_json
        result
      end
    end

    def redis= url
      @redis = nil
      set :redis_url, url
      redis
    end

    def redis
      @redis ||= EventMachine::Synchrony::ConnectionPool.new(size: redis_pool_size) do
        Redis.new driver: :synchrony, url: redis_url
      end
    end

    def self.registered app
      app.helpers RedisCache::Helpers
      
      app.set :redis_url, lambda { ENV['REDIS_URL'] || 'redis://127.0.0.1:6379' }
      app.set :redis_pool_size, 2

      app.redis

    end
  end

  register RedisCache
end